package net.feedbacky.app.service.idea;

import net.feedbacky.app.config.UserAuthenticationToken;
import net.feedbacky.app.data.board.Board;
import net.feedbacky.app.data.board.moderator.Moderator;
import net.feedbacky.app.data.board.webhook.Webhook;
import net.feedbacky.app.data.board.webhook.WebhookDataBuilder;
import net.feedbacky.app.data.idea.Idea;
import net.feedbacky.app.data.idea.comment.Comment;
import net.feedbacky.app.data.idea.dto.FetchIdeaDto;
import net.feedbacky.app.data.idea.dto.PatchIdeaDto;
import net.feedbacky.app.data.idea.dto.PostIdeaDto;
import net.feedbacky.app.data.idea.subscribe.NotificationEvent;
import net.feedbacky.app.data.idea.subscribe.SubscriptionExecutor;
import net.feedbacky.app.data.tag.Tag;
import net.feedbacky.app.data.tag.dto.FetchTagDto;
import net.feedbacky.app.data.tag.dto.PatchTagRequestDto;
import net.feedbacky.app.data.user.User;
import net.feedbacky.app.data.user.dto.FetchSimpleUserDto;
import net.feedbacky.app.data.user.dto.FetchUserDto;
import net.feedbacky.app.exception.FeedbackyRestException;
import net.feedbacky.app.exception.types.InvalidAuthenticationException;
import net.feedbacky.app.exception.types.ResourceNotFoundException;
import net.feedbacky.app.repository.UserRepository;
import net.feedbacky.app.repository.board.BoardRepository;
import net.feedbacky.app.repository.board.TagRepository;
import net.feedbacky.app.repository.idea.CommentRepository;
import net.feedbacky.app.repository.idea.IdeaRepository;
import net.feedbacky.app.service.ServiceUser;
import net.feedbacky.app.util.CommentBuilder;
import net.feedbacky.app.util.PaginableRequest;
import net.feedbacky.app.util.objectstorage.ObjectStorage;
import net.feedbacky.app.util.request.InternalRequestValidator;

import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphUtils;
import com.cosium.spring.data.jpa.entity.graph.domain.EntityGraphs;

import org.apache.commons.text.StringEscapeUtils;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Plajer
 * <p>
 * Created at 11.10.2019
 */
@Service
public class IdeaServiceImpl implements IdeaService {

  private final IdeaRepository ideaRepository;
  private final BoardRepository boardRepository;
  private final UserRepository userRepository;
  private final TagRepository tagRepository;
  private final CommentRepository commentRepository;
  private final ObjectStorage objectStorage;
  private final SubscriptionExecutor subscriptionExecutor;
  private final IdeaServiceCommons ideaServiceCommons;

  @Autowired
  //todo too big constructor
  public IdeaServiceImpl(IdeaRepository ideaRepository, BoardRepository boardRepository, UserRepository userRepository, TagRepository tagRepository,
                         CommentRepository commentRepository, ObjectStorage objectStorage,
                         SubscriptionExecutor subscriptionExecutor, IdeaServiceCommons ideaServiceCommons) {
    this.ideaRepository = ideaRepository;
    this.boardRepository = boardRepository;
    this.userRepository = userRepository;
    this.tagRepository = tagRepository;
    this.commentRepository = commentRepository;
    this.objectStorage = objectStorage;
    this.subscriptionExecutor = subscriptionExecutor;
    this.ideaServiceCommons = ideaServiceCommons;
  }

  @Override
  public PaginableRequest<List<FetchIdeaDto>> getAllIdeas(String discriminator, int page, int pageSize, FilterType filter, SortType sort) {
    User user = null;
    if(SecurityContextHolder.getContext().getAuthentication() instanceof UserAuthenticationToken) {
      UserAuthenticationToken auth = (UserAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
      user = userRepository.findByEmail(((ServiceUser) auth.getPrincipal()).getEmail()).orElse(null);
    }
    Board board = boardRepository.findByDiscriminator(discriminator)
            .orElseThrow(() -> new ResourceNotFoundException("Board with discriminator " + discriminator + " does not exist."));
    return ideaServiceCommons.getAllIdeas(board, user, page, pageSize, filter, sort);
  }

  @Override
  public PaginableRequest<List<FetchIdeaDto>> getAllIdeasContaining(String discriminator, int page, int pageSize, String query) {
    User user = null;
    if(SecurityContextHolder.getContext().getAuthentication() instanceof UserAuthenticationToken) {
      UserAuthenticationToken auth = (UserAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
      user = userRepository.findByEmail(((ServiceUser) auth.getPrincipal()).getEmail()).orElse(null);
    }
    Board board = boardRepository.findByDiscriminator(discriminator)
            .orElseThrow(() -> new ResourceNotFoundException("Board with discriminator " + discriminator + " does not exist."));
    return ideaServiceCommons.getAllIdeasContaining(board, user, page, pageSize, query);
  }

  @Override
  public FetchIdeaDto getOne(long id) {
    User user = null;
    if(SecurityContextHolder.getContext().getAuthentication() instanceof UserAuthenticationToken) {
      UserAuthenticationToken auth = (UserAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
      user = userRepository.findByEmail(((ServiceUser) auth.getPrincipal()).getEmail()).orElse(null);
    }
    return ideaServiceCommons.getOne(user, id);
  }

  @Override
  public ResponseEntity<FetchIdeaDto> post(PostIdeaDto dto) {
    UserAuthenticationToken auth = InternalRequestValidator.getContextAuthentication();
    User user = userRepository.findByEmail(((ServiceUser) auth.getPrincipal()).getEmail())
            .orElseThrow(() -> new InvalidAuthenticationException("Session not found. Try again with new token."));
    Board board = boardRepository.findByDiscriminator(dto.getDiscriminator())
            .orElseThrow(() -> new ResourceNotFoundException("Board with discriminator " + dto.getDiscriminator() + " not found."));
    return ResponseEntity.status(HttpStatus.CREATED).body(ideaServiceCommons.post(dto, board, user));
  }

  @Override
  public FetchIdeaDto patch(long id, PatchIdeaDto dto) {
    UserAuthenticationToken auth = InternalRequestValidator.getContextAuthentication();
    User user = userRepository.findByEmail(((ServiceUser) auth.getPrincipal()).getEmail())
            .orElseThrow(() -> new InvalidAuthenticationException("Session not found. Try again with new token."));
    Idea idea = ideaRepository.findById(id, EntityGraphs.named("Idea.fetch"))
            .orElseThrow(() -> new ResourceNotFoundException(MessageFormat.format("Idea with id {0} not found.", id)));
    if(dto.getOpen() != null && !hasPermission(idea.getBoard(), Moderator.Role.MODERATOR, user)) {
      throw new InvalidAuthenticationException("Insufficient permissions.");
    }
    if(!idea.getCreator().equals(user) && !hasPermission(idea.getBoard(), Moderator.Role.MODERATOR, user)) {
      throw new InvalidAuthenticationException("Insufficient permissions.");
    }

    boolean edited = false;
    long creationTimeDiffMillis = Math.abs(Calendar.getInstance().getTime().getTime() - idea.getCreationDate().getTime());
    long minutesDiff = TimeUnit.MINUTES.convert(creationTimeDiffMillis, TimeUnit.MILLISECONDS);
    //mark ideas edited only if they were posted later than 5 minutes for any typo fixes etc.
    if(dto.getDescription() != null && !idea.getDescription().equals(StringEscapeUtils.escapeHtml4(dto.getDescription())) && minutesDiff > 5) {
      edited = true;
      idea.setEdited(true);
    }
    Comment comment = null;
    //assuming you can never close and edit idea in the same request
    if(edited) {
      comment = new CommentBuilder()
              .of(idea)
              .by(user)
              .type(Comment.SpecialType.IDEA_EDITED)
              .message(user.convertToSpecialCommentMention() + " has edited description of the idea.")
              .build();
      WebhookDataBuilder builder = new WebhookDataBuilder().withUser(user).withIdea(idea).withComment(comment);
      idea.getBoard().getWebhookExecutor().executeWebhooks(Webhook.Event.IDEA_EDIT, builder.build());
    } else if(dto.getOpen() != null && idea.getStatus().getValue() != dto.getOpen()) {
      if(!dto.getOpen()) {
        comment = new CommentBuilder()
                .of(idea)
                .by(user)
                .type(Comment.SpecialType.IDEA_CLOSED)
                .message(user.convertToSpecialCommentMention() + " has closed the idea.")
                .build();
        WebhookDataBuilder builder = new WebhookDataBuilder().withUser(user).withIdea(idea).withComment(comment);
        idea.getBoard().getWebhookExecutor().executeWebhooks(Webhook.Event.IDEA_CLOSE, builder.build());
      } else {
        comment = new CommentBuilder()
                .of(idea)
                .by(user)
                .type(Comment.SpecialType.IDEA_OPENED)
                .message(user.convertToSpecialCommentMention() + " has reopened the idea.")
                .build();
        WebhookDataBuilder builder = new WebhookDataBuilder().withUser(user).withIdea(idea).withComment(comment);
        idea.getBoard().getWebhookExecutor().executeWebhooks(Webhook.Event.IDEA_OPEN, builder.build());
      }
      subscriptionExecutor.notifySubscribers(idea, new NotificationEvent(SubscriptionExecutor.Event.IDEA_STATUS_CHANGE,
              idea, idea.getStatus().name()));
    }
    ModelMapper mapper = new ModelMapper();
    mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    mapper.map(dto, idea);
    if(dto.getOpen() != null) {
      idea.setStatus(Idea.IdeaStatus.toIdeaStatus(dto.getOpen()));
    }
    idea.setDescription(StringEscapeUtils.escapeHtml4(idea.getDescription()));
    if(comment != null) {
      idea.getComments().add(comment);
      commentRepository.save(comment);
    }
    ideaRepository.save(idea);
    return new FetchIdeaDto().from(idea).withUser(idea, user);
  }

  @Override
  public ResponseEntity delete(long id) {
    UserAuthenticationToken auth = InternalRequestValidator.getContextAuthentication();
    User user = userRepository.findByEmail(((ServiceUser) auth.getPrincipal()).getEmail())
            .orElseThrow(() -> new InvalidAuthenticationException("Session not found. Try again with new token."));
    Idea idea = ideaRepository.findById(id, EntityGraphs.named("Idea.fetch"))
            .orElseThrow(() -> new ResourceNotFoundException(MessageFormat.format("Idea with id {0} not found.", id)));
    if(!idea.getCreator().equals(user) && !hasPermission(idea.getBoard(), Moderator.Role.MODERATOR, user)) {
      throw new InvalidAuthenticationException("Insufficient permissions.");
    }
    idea.getAttachments().forEach(attachment -> objectStorage.deleteImage(attachment.getUrl()));
    WebhookDataBuilder builder = new WebhookDataBuilder().withUser(user).withIdea(idea);
    idea.getBoard().getWebhookExecutor().executeWebhooks(Webhook.Event.IDEA_DELETE, builder.build());
    ideaRepository.delete(idea);
    return ResponseEntity.noContent().build();
  }

  @Override
  public List<FetchSimpleUserDto> getAllVoters(long id) {
    Idea idea = ideaRepository.findById(id, EntityGraphUtils.fromAttributePaths("voters"))
            .orElseThrow(() -> new ResourceNotFoundException("Idea with id " + id + " does not exist."));
    return idea.getVoters().stream().map(usr -> new FetchSimpleUserDto().from(usr)).collect(Collectors.toList());
  }

  @Override
  public FetchUserDto postUpvote(long id) {
    //todo X-User-Id vote on behalf
    UserAuthenticationToken auth = InternalRequestValidator.getContextAuthentication();
    User user = userRepository.findByEmail(((ServiceUser) auth.getPrincipal()).getEmail())
            .orElseThrow(() -> new InvalidAuthenticationException("Session not found. Try again with new token."));
    Idea idea = ideaRepository.findById(id, EntityGraphs.named("Idea.fetch"))
            .orElseThrow(() -> new ResourceNotFoundException(MessageFormat.format("Idea with id {0} not found.", id)));
    if(idea.getVoters().contains(user)) {
      throw new FeedbackyRestException(HttpStatus.BAD_REQUEST, "Already upvoted.");
    }
    if(idea.getBoard().getSuspensedList().stream().anyMatch(suspended -> suspended.getUser().equals(user))) {
      throw new FeedbackyRestException(HttpStatus.BAD_REQUEST, "You've been suspended, please contact board owner for more information.");
    }
    Set<User> voters = idea.getVoters();
    voters.add(user);
    idea.setVoters(voters);
    ideaRepository.save(idea);
    return new FetchUserDto().from(user);
  }

  @Override
  public ResponseEntity deleteUpvote(long id) {
    UserAuthenticationToken auth = InternalRequestValidator.getContextAuthentication();
    User user = userRepository.findByEmail(((ServiceUser) auth.getPrincipal()).getEmail())
            .orElseThrow(() -> new InvalidAuthenticationException("Session not found. Try again with new token."));
    Idea idea = ideaRepository.findById(id, EntityGraphs.named("Idea.fetch"))
            .orElseThrow(() -> new ResourceNotFoundException(MessageFormat.format("Idea with id {0} not found.", id)));
    if(!idea.getVoters().contains(user)) {
      throw new FeedbackyRestException(HttpStatus.BAD_REQUEST, "Not yet upvoted.");
    }
    if(idea.getBoard().getSuspensedList().stream().anyMatch(suspended -> suspended.getUser().equals(user))) {
      throw new FeedbackyRestException(HttpStatus.BAD_REQUEST, "You've been suspended, please contact board owner for more information.");
    }
    Set<User> voters = idea.getVoters();
    voters.remove(user);
    idea.setVoters(voters);
    ideaRepository.save(idea);
    return ResponseEntity.noContent().build();
  }

  @Override
  public List<FetchTagDto> patchTags(long id, List<PatchTagRequestDto> tags) {
    UserAuthenticationToken auth = InternalRequestValidator.getContextAuthentication();
    User user = userRepository.findByEmail(((ServiceUser) auth.getPrincipal()).getEmail())
            .orElseThrow(() -> new InvalidAuthenticationException("Session not found. Try again with new token."));
    Idea idea = ideaRepository.findById(id, EntityGraphs.named("Idea.fetch"))
            .orElseThrow(() -> new ResourceNotFoundException(MessageFormat.format("Idea with id {0} not found.", id)));
    if(!hasPermission(idea.getBoard(), Moderator.Role.MODERATOR, user)) {
      throw new InvalidAuthenticationException("Insufficient permissions.");
    }
    if(tags.isEmpty()) {
      throw new FeedbackyRestException(HttpStatus.BAD_REQUEST, "No changes made.");
    }
    List<Tag> addedTags = new ArrayList<>();
    List<Tag> removedTags = new ArrayList<>();
    for(PatchTagRequestDto preTag : tags) {
      Tag tag = tagRepository.findByBoardAndName(idea.getBoard(), preTag.getName())
              .orElseThrow(() -> new ResourceNotFoundException(MessageFormat.format("Tag with name {0} not found.", preTag.getName())));
      for(Tag ideaTag : idea.getBoard().getTags()) {
        if(!ideaTag.getName().equals(preTag.getName())) {
          continue;
        }
        if(preTag.getApply() && !idea.getTags().contains(tag)) {
          addedTags.add(tag);
        } else if(!preTag.getApply() && idea.getTags().contains(tag)) {
          removedTags.add(tag);
        }
      }
    }
    if(removedTags.isEmpty() && addedTags.isEmpty()) {
      throw new FeedbackyRestException(HttpStatus.BAD_REQUEST, "No changes made.");
    }
    Comment comment = prepareTagsPatchComment(user, idea, addedTags, removedTags);
    idea.getComments().add(comment);
    commentRepository.save(comment);
    ideaRepository.save(idea);
    WebhookDataBuilder webhookBuilder = new WebhookDataBuilder().withUser(user).withIdea(comment.getIdea())
            .withTagsChangedData(prepareTagChangeMessage(user, idea, addedTags, removedTags, false));
    idea.getBoard().getWebhookExecutor().executeWebhooks(Webhook.Event.IDEA_TAG_CHANGE, webhookBuilder.build());

    subscriptionExecutor.notifySubscribers(idea, new NotificationEvent(SubscriptionExecutor.Event.IDEA_TAGS_CHANGE,
            idea, prepareTagChangeMessage(user, idea, addedTags, removedTags, false)));
    return idea.getTags().stream().map(tag -> new FetchTagDto().from(tag)).collect(Collectors.toList());
  }

  private Comment prepareTagsPatchComment(User user, Idea idea, List<Tag> addedTags, List<Tag> removedTags) {
    return new CommentBuilder()
            .of(idea)
            .by(user)
            .type(Comment.SpecialType.TAGS_MANAGED)
            .message(prepareTagChangeMessage(user, idea, addedTags, removedTags, true))
            .build();
  }

  private String prepareTagChangeMessage(User user, Idea idea, List<Tag> addedTags, List<Tag> removedTags, boolean tagDataDisplay) {
    String userName;
    if(tagDataDisplay) {
      userName = user.convertToSpecialCommentMention();
    } else {
      userName = user.getUsername();
    }
    StringBuilder builder = new StringBuilder(userName + " has ");
    if(!addedTags.isEmpty()) {
      builder.append("added");
      for(Tag tag : addedTags) {
        idea.getTags().add(tag);
        builder.append(" ");
        if(tagDataDisplay) {
          builder.append(tag.convertToSpecialCommentMention());
        } else {
          builder.append("`").append(tag.getName()).append("`");
        }
        builder.append(" ");
      }
      if(addedTags.size() == 1) {
        builder.append("tag");
      } else {
        builder.append("tags");
      }
    }
    if(!removedTags.isEmpty()) {
      //tags were added
      if(!builder.toString().endsWith("has ")) {
        builder.append(" and ");
      }
      builder.append("removed");
      for(Tag tag : removedTags) {
        idea.getTags().remove(tag);
        builder.append(" ");
        if(tagDataDisplay) {
          builder.append(tag.convertToSpecialCommentMention());
        } else {
          builder.append("`").append(tag.getName()).append("`");
        }
        builder.append(" ");
      }
      if(removedTags.size() == 1) {
        builder.append("tag");
      } else {
        builder.append("tags");
      }
    }
    return builder.toString();
  }

}
