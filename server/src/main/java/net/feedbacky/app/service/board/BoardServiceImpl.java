package net.feedbacky.app.service.board;

import net.feedbacky.app.config.UserAuthenticationToken;
import net.feedbacky.app.data.board.Board;
import net.feedbacky.app.data.board.dto.FetchBoardDto;
import net.feedbacky.app.data.board.dto.PatchBoardDto;
import net.feedbacky.app.data.board.dto.PostBoardDto;
import net.feedbacky.app.data.board.moderator.Moderator;
import net.feedbacky.app.data.idea.Idea;
import net.feedbacky.app.data.tag.Tag;
import net.feedbacky.app.data.tag.dto.FetchTagDto;
import net.feedbacky.app.data.tag.dto.PatchTagDto;
import net.feedbacky.app.data.tag.dto.PostTagDto;
import net.feedbacky.app.data.user.User;
import net.feedbacky.app.exception.FeedbackyRestException;
import net.feedbacky.app.exception.types.InvalidAuthenticationException;
import net.feedbacky.app.exception.types.ResourceNotFoundException;
import net.feedbacky.app.repository.UserRepository;
import net.feedbacky.app.repository.board.BoardRepository;
import net.feedbacky.app.repository.board.TagRepository;
import net.feedbacky.app.repository.idea.IdeaRepository;
import net.feedbacky.app.service.ServiceUser;
import net.feedbacky.app.service.board.featured.FeaturedBoardsServiceImpl;
import net.feedbacky.app.util.Base64Util;
import net.feedbacky.app.util.Constants;
import net.feedbacky.app.util.PaginableRequest;
import net.feedbacky.app.util.mailservice.MailBuilder;
import net.feedbacky.app.util.request.InternalRequestValidator;
import net.feedbacky.app.util.mailservice.MailHandler;
import net.feedbacky.app.util.mailservice.MailService;
import net.feedbacky.app.util.objectstorage.ObjectStorage;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Plajer
 * <p>
 * Created at 10.10.2019
 */
@Service
public class BoardServiceImpl implements BoardService {

  private final BoardRepository boardRepository;
  private final UserRepository userRepository;
  private final IdeaRepository ideaRepository;
  private final TagRepository tagRepository;
  private final ObjectStorage objectStorage;
  private final MailHandler mailHandler;
  private final FeaturedBoardsServiceImpl featuredBoardsServiceImpl;

  @Autowired
  //todo too big constuctor
  public BoardServiceImpl(BoardRepository boardRepository, UserRepository userRepository, IdeaRepository ideaRepository, TagRepository tagRepository,
                          ObjectStorage objectStorage, MailHandler mailHandler, FeaturedBoardsServiceImpl featuredBoardsServiceImpl) {
    this.boardRepository = boardRepository;
    this.userRepository = userRepository;
    this.ideaRepository = ideaRepository;
    this.tagRepository = tagRepository;
    this.objectStorage = objectStorage;
    this.mailHandler = mailHandler;
    this.featuredBoardsServiceImpl = featuredBoardsServiceImpl;
  }

  @Override
  public PaginableRequest<List<FetchBoardDto>> getAll(int page, int pageSize) {
    Page<Board> pageData = boardRepository.findAll(PageRequest.of(page, pageSize));
    List<Board> boards = pageData.getContent();
    int totalPages = pageData.getTotalElements() == 0 ? 0 : pageData.getTotalPages() - 1;
    return new PaginableRequest<>(new PaginableRequest.PageMetadata(page, totalPages, pageSize),
            boards.stream().map(board -> board.convertToDto().exposeSensitiveData(false)).collect(Collectors.toList()));
  }

  @Override
  public FetchBoardDto getOne(String discriminator) {
    User user = null;
    if(SecurityContextHolder.getContext().getAuthentication() instanceof UserAuthenticationToken) {
      UserAuthenticationToken auth = (UserAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
      user = userRepository.findByEmail(((ServiceUser) auth.getPrincipal()).getEmail()).orElse(null);
    }
    Board board = boardRepository.findByDiscriminator(discriminator)
            .orElseThrow(() -> new ResourceNotFoundException("Board with discriminator " + discriminator + " not found"));
    return board.convertToDto().exposeSensitiveData(board.getCreator().equals(user));
  }

  @Override
  public ResponseEntity<FetchBoardDto> post(PostBoardDto dto) {
    UserAuthenticationToken auth = InternalRequestValidator.getContextAuthentication();
    User user = userRepository.findByEmail(((ServiceUser) auth.getPrincipal()).getEmail())
            .orElseThrow(() -> new InvalidAuthenticationException("User session not found. Try again with new token."));
    if(boardRepository.findByDiscriminator(dto.getDiscriminator()).isPresent()) {
      throw new FeedbackyRestException(HttpStatus.BAD_REQUEST, "Board with that discriminator already exists.");
    }
    Board board = dto.convertToEntity();

    //sanitize
    board.setShortDescription(StringEscapeUtils.escapeHtml4(dto.getShortDescription()));
    board.setFullDescription(StringEscapeUtils.escapeHtml4(dto.getFullDescription()));

    board.setCreator(user);

    //after save board id is set, so now we can set banners and logos that require board id
    if(dto.getLogo() != null) {
      String logoUrl = objectStorage.storeImage(Base64Util.extractBase64Data(dto.getLogo()), ObjectStorage.ImageType.PROJECT_LOGO);
      if(logoUrl.equals("")) {
        throw new FeedbackyRestException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to handle board logo due to server side error.");
      }
      board.setLogo(logoUrl);
    } else {
      board.setLogo(Constants.DEFAULT_LOGO_URL);
    }
    if(dto.getBanner() != null) {
      String bannerUrl = objectStorage.storeImage(Base64Util.extractBase64Data(dto.getBanner()), ObjectStorage.ImageType.PROJECT_BANNER);
      if(bannerUrl.equals("")) {
        throw new FeedbackyRestException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to handle board banner due to server side error.");
      }
      board.setBanner(bannerUrl);
    } else {
      board.setBanner(Constants.DEFAULT_BANNER_URL);
    }
    board = boardRepository.save(board);

    Moderator node = new Moderator();
    node.setRole(Moderator.Role.OWNER);
    node.setBoard(board);
    node.setUser(user);
    user.getPermissions().add(node);
    userRepository.save(user);
    populateBoardWithExamples(board);
    return ResponseEntity.status(HttpStatus.CREATED).body(board.convertToDto().exposeSensitiveData(true));
  }


  private void populateBoardWithExamples(Board board) {
    Tag tag = new Tag();
    tag.setBoard(board);
    tag.setColor("#2d2d2d");
    tag.setName("Example");
    tag = tagRepository.save(tag);

    User admin = userRepository.findByServiceStaffTrue().get(0);
    Idea welcome = new Idea();
    welcome.setBoard(board);
    welcome.setTitle("Welcome to Feedbacky");
    welcome.setDescription("This is your personal board for your project, feel free to edit it's settings in admin panel. You can also remove this idea via moderation tools icon above.\n"
            + "\n"
            + "**TIP:** You can use *markdown* and emojis here :)");
    welcome.setCreator(admin);
    welcome.setCreationDate(Calendar.getInstance().getTime());
    welcome.setVoters(new HashSet<>());
    welcome.setStatus(Idea.IdeaStatus.OPENED);
    Set<Tag> tags = new HashSet<>();
    tags.add(tag);
    welcome.setTags(tags);
    welcome.setAttachments(new HashSet<>());
    ideaRepository.save(welcome);

    Idea connect = new Idea();
    connect.setBoard(board);
    connect.setTitle("Connect the board with your project");
    connect.setDescription("Create social links to refer to your page, Discord server or Patreon in admin panel.\n"
            + "You can create Discord webhook as well to receive notifications about new feedback being posted here at your Discord server.");
    connect.setCreator(admin);
    connect.setCreationDate(Calendar.getInstance().getTime());
    connect.setVoters(new HashSet<>());
    connect.setStatus(Idea.IdeaStatus.OPENED);
    connect.setTags(new HashSet<>());
    connect.setAttachments(new HashSet<>());
    ideaRepository.save(connect);
  }

  @Override
  public FetchBoardDto patch(String discriminator, PatchBoardDto dto) {
    UserAuthenticationToken auth = InternalRequestValidator.getContextAuthentication();
    User user = userRepository.findByEmail(((ServiceUser) auth.getPrincipal()).getEmail())
            .orElseThrow(() -> new InvalidAuthenticationException("User session not found. Try again with new token"));
    Board board = boardRepository.findByDiscriminator(discriminator)
            .orElseThrow(() -> new ResourceNotFoundException("Board with discriminator " + discriminator + " does not exist."));
    if(!hasPermission(board, Moderator.Role.OWNER, user)) {
      throw new InvalidAuthenticationException("No permission to patch board with discriminator " + discriminator + ".");
    }

    //convert and update base64 images
    if(dto.getBanner() != null) {
      //delete old banner if necessary
      objectStorage.deleteImage(board.getBanner());
      String bannerUrl = objectStorage.storeImage(Base64Util.extractBase64Data(dto.getBanner()), ObjectStorage.ImageType.PROJECT_BANNER);
      if(bannerUrl.equals("")) {
        throw new FeedbackyRestException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to handle board banner due to server side error.");
      }
      dto.setBanner(bannerUrl);
    }
    if(dto.getLogo() != null) {
      //delete old logo if necessary
      objectStorage.deleteImage(board.getLogo());
      String logoUrl = objectStorage.storeImage(Base64Util.extractBase64Data(dto.getLogo()), ObjectStorage.ImageType.PROJECT_LOGO);
      if(logoUrl.equals("")) {
        throw new FeedbackyRestException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to handle board logo due to server side error.");
      }
      dto.setLogo(logoUrl);
    }
    ModelMapper mapper = new ModelMapper();
    mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    mapper.map(dto, board);

    //sanitize
    board.setShortDescription(StringEscapeUtils.escapeHtml4(board.getShortDescription()));
    board.setFullDescription(StringEscapeUtils.escapeHtml4(board.getFullDescription()));

    boardRepository.save(board);
    return board.convertToDto().exposeSensitiveData(true);
  }

  @Override
  public ResponseEntity delete(String discriminator) {
    UserAuthenticationToken auth = InternalRequestValidator.getContextAuthentication();
    User user = userRepository.findByEmail(((ServiceUser) auth.getPrincipal()).getEmail())
            .orElseThrow(() -> new InvalidAuthenticationException("User session not found. Try again with new token"));
    Board board = boardRepository.findByDiscriminator(discriminator)
            .orElseThrow(() -> new ResourceNotFoundException("Board with discriminator " + discriminator + " does not exist."));
    if(!hasPermission(board, Moderator.Role.OWNER, user)) {
      throw new InvalidAuthenticationException("No permission to delete board with discriminator " + discriminator + ".");
    }
    new MailBuilder()
            .withRecipient(board.getCreator())
            .withEventBoard(board)
            .withTemplate(MailService.EmailTemplate.BOARD_DELETED)
            .sendMail(mailHandler.getMailService()).sync();

    board.getModerators().forEach(moderator -> {
      User modUser = moderator.getUser();
      modUser.getPermissions().remove(moderator);
      userRepository.save(modUser);
    });
    board.getSocialLinks().forEach(link -> objectStorage.deleteImage(link.getLogoUrl()));
    board.getIdeas().forEach(idea -> idea.getAttachments().forEach(attachment -> objectStorage.deleteImage(attachment.getUrl())));
    objectStorage.deleteImage(board.getBanner());
    objectStorage.deleteImage(board.getLogo());
    boardRepository.delete(board);
    //call explicitly to update boards if featured boards contained deleted board
    featuredBoardsServiceImpl.scheduleFeaturedBoardsSelectionTask();
    return ResponseEntity.noContent().build();
  }

  @Override
  public List<FetchTagDto> getAllTags(String discriminator) {
    Board board = boardRepository.findByDiscriminator(discriminator)
            .orElseThrow(() -> new ResourceNotFoundException("Board with discriminator " + discriminator + " does not exist."));
    return board.getTags().stream().map(Tag::convertToDto).collect(Collectors.toList());
  }

  @Override
  public FetchTagDto getTagByName(String discriminator, String name) {
    Board board = boardRepository.findByDiscriminator(discriminator)
            .orElseThrow(() -> new ResourceNotFoundException("Board with discriminator " + discriminator + " does not exist."));
    Tag tag = tagRepository.findByBoardAndName(board, name)
            .orElseThrow(() -> new ResourceNotFoundException("Tag with name " + name + " does not exist."));
    return tag.convertToDto();
  }

  @Override
  public ResponseEntity<FetchTagDto> postTag(String discriminator, PostTagDto dto) {
    UserAuthenticationToken auth = InternalRequestValidator.getContextAuthentication();
    User user = userRepository.findByEmail(((ServiceUser) auth.getPrincipal()).getEmail())
            .orElseThrow(() -> new InvalidAuthenticationException("User session not found. Try again with new token"));
    Board board = boardRepository.findByDiscriminator(discriminator)
            .orElseThrow(() -> new ResourceNotFoundException("Board with discriminator " + discriminator + " does not exist."));
    if(!hasPermission(board, Moderator.Role.OWNER, user)) {
      throw new InvalidAuthenticationException("No permission to post new tags to board with discriminator " + discriminator + ".");
    }
    if(board.getTags().size() >= 10) {
      throw new FeedbackyRestException(HttpStatus.FORBIDDEN, "Cannot add more than 10 tags to the board.");
    }
    if(tagRepository.findByBoardAndName(board, dto.getName()).isPresent()) {
      throw new FeedbackyRestException(HttpStatus.BAD_REQUEST, "This tag already exists in the board.");
    }
    Tag tag = dto.convertToEntity(board);
    tagRepository.save(tag);
    return ResponseEntity.status(HttpStatus.CREATED).body(tag.convertToDto());
  }

  @Override
  public FetchTagDto patchTag(String discriminator, String name, PatchTagDto dto) {
    UserAuthenticationToken auth = InternalRequestValidator.getContextAuthentication();
    User user = userRepository.findByEmail(((ServiceUser) auth.getPrincipal()).getEmail())
            .orElseThrow(() -> new InvalidAuthenticationException("User session not found. Try again with new token"));
    Board board = boardRepository.findByDiscriminator(discriminator)
            .orElseThrow(() -> new ResourceNotFoundException("Board with discriminator " + discriminator + " does not exist."));
    if(!hasPermission(board, Moderator.Role.OWNER, user)) {
      throw new InvalidAuthenticationException("No permission to patch tags to board with discriminator " + discriminator + ".");
    }
    Tag tag = tagRepository.findByBoardAndName(board, name)
            .orElseThrow(() -> new ResourceNotFoundException("Tag with name " + name + " does not exist."));

    ModelMapper mapper = new ModelMapper();
    mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    mapper.map(dto, tag);

    tagRepository.save(tag);
    return tag.convertToDto();
  }

  @Override
  public ResponseEntity deleteTag(String discriminator, String name) {
    UserAuthenticationToken auth = InternalRequestValidator.getContextAuthentication();
    User user = userRepository.findByEmail(((ServiceUser) auth.getPrincipal()).getEmail())
            .orElseThrow(() -> new InvalidAuthenticationException("User session not found. Try again with new token"));
    Board board = boardRepository.findByDiscriminator(discriminator)
            .orElseThrow(() -> new ResourceNotFoundException("Board with discriminator " + discriminator + " does not exist."));
    if(!hasPermission(board, Moderator.Role.OWNER, user)) {
      throw new InvalidAuthenticationException("No permission to patch tags to board with discriminator " + discriminator + ".");
    }
    Tag tag = tagRepository.findByBoardAndName(board, name)
            .orElseThrow(() -> new ResourceNotFoundException("Tag with name " + name + " does not exist."));
    board.getIdeas().forEach(idea -> {
      idea.getTags().remove(tag);
      ideaRepository.save(idea);
    });
    tagRepository.delete(tag);
    return ResponseEntity.noContent().build();
  }
}
