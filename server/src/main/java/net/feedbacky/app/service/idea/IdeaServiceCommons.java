package net.feedbacky.app.service.idea;

import net.feedbacky.app.data.board.Board;
import net.feedbacky.app.data.board.webhook.Webhook;
import net.feedbacky.app.data.board.webhook.WebhookDataBuilder;
import net.feedbacky.app.data.idea.Idea;
import net.feedbacky.app.data.idea.attachment.Attachment;
import net.feedbacky.app.data.idea.dto.FetchIdeaDto;
import net.feedbacky.app.data.idea.dto.PostIdeaDto;
import net.feedbacky.app.data.user.User;
import net.feedbacky.app.data.user.dto.FetchUserDto;
import net.feedbacky.app.exception.FeedbackyRestException;
import net.feedbacky.app.exception.types.InvalidAuthenticationException;
import net.feedbacky.app.exception.types.ResourceNotFoundException;
import net.feedbacky.app.repository.idea.AttachmentRepository;
import net.feedbacky.app.repository.idea.IdeaRepository;
import net.feedbacky.app.service.ServiceUser;
import net.feedbacky.app.util.Base64Util;
import net.feedbacky.app.util.PaginableRequest;
import net.feedbacky.app.util.SortFilterResolver;
import net.feedbacky.app.util.objectstorage.ObjectStorage;

import org.apache.commons.text.StringEscapeUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Plajer
 * <p>
 * Created at 13.08.2020
 */
@Component
public class IdeaServiceCommons {

  private IdeaRepository ideaRepository;
  private ObjectStorage objectStorage;
  private AttachmentRepository attachmentRepository;

  @Autowired
  public IdeaServiceCommons(IdeaRepository ideaRepository, ObjectStorage objectStorage, AttachmentRepository attachmentRepository) {
    this.ideaRepository = ideaRepository;
    this.objectStorage = objectStorage;
    this.attachmentRepository = attachmentRepository;
  }

  public PaginableRequest<List<FetchIdeaDto>> getAllIdeas(Board board, User user, int page, int pageSize, IdeaService.FilterType filter, IdeaService.SortType sort) {
    //not using board.getIdeas() because it would load all, we need paged limited list
    Page<Idea> pageData;
    switch(filter) {
      case OPENED:
        pageData = ideaRepository.findByBoardAndStatus(board, Idea.IdeaStatus.OPENED, PageRequest.of(page, pageSize, SortFilterResolver.resolveIdeaSorting(sort)));
        break;
      case CLOSED:
        pageData = ideaRepository.findByBoardAndStatus(board, Idea.IdeaStatus.CLOSED, PageRequest.of(page, pageSize, SortFilterResolver.resolveIdeaSorting(sort)));
        break;
      case ALL:
        pageData = ideaRepository.findByBoard(board, PageRequest.of(page, pageSize, SortFilterResolver.resolveIdeaSorting(sort)));
        break;
      default:
        throw new FeedbackyRestException(HttpStatus.BAD_REQUEST, "Invalid filter type.");
    }
    List<Idea> ideas = pageData.getContent();
    final User finalUser = user;
    int totalPages = pageData.getTotalElements() == 0 ? 0 : pageData.getTotalPages() - 1;
    return new PaginableRequest<>(new PaginableRequest.PageMetadata(page, totalPages, pageSize), ideas.stream()
            .map(idea -> idea.convertToDto(finalUser)).collect(Collectors.toList()));
  }

  public PaginableRequest<List<FetchIdeaDto>> getAllIdeasContaining(Board board, User user, int page, int pageSize, String query) {
    final User finalUser = user;
    Page<Idea> pageData = ideaRepository.findByBoardAndTitleIgnoreCaseContaining(board, query, PageRequest.of(page, pageSize));
    List<Idea> ideas = pageData.getContent();
    int totalPages = pageData.getTotalElements() == 0 ? 0 : pageData.getTotalPages() - 1;
    return new PaginableRequest<>(new PaginableRequest.PageMetadata(page, totalPages, pageSize), ideas.stream()
            .map(idea -> idea.convertToDto(finalUser)).collect(Collectors.toList()));
  }

  public FetchIdeaDto getOne(User user, long id) {
    return ideaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Idea with id " + id + " not found"))
            .convertToDto(user);
  }

  public FetchIdeaDto post(PostIdeaDto dto, Board board, User user) {
    Optional<Idea> optional = ideaRepository.findByTitleAndBoard(dto.getTitle(), board);
    if(optional.isPresent() && optional.get().getBoard().getId().equals(board.getId())) {
      throw new FeedbackyRestException(HttpStatus.BAD_REQUEST, "Idea with that title in that board already exists.");
    }
    if(board.getSuspensedList().stream().anyMatch(suspended -> suspended.getUser().equals(user))) {
      throw new FeedbackyRestException(HttpStatus.BAD_REQUEST, "You've been suspended, please contact board owner for more information.");
    }
    ModelMapper mapper = new ModelMapper();
    Idea idea = mapper.map(dto, Idea.class);
    idea.setId(null);
    idea.setBoard(board);
    idea.setCreator(user);
    idea.setCreationDate(Calendar.getInstance().getTime());
    Set<User> set = new HashSet<>();
    set.add(user);
    idea.setVoters(set);
    idea.setStatus(Idea.IdeaStatus.OPENED);
    idea.setDescription(StringEscapeUtils.escapeHtml4(idea.getDescription()));
    idea.setSubscribers(set);
    idea = ideaRepository.save(idea);

    //must save idea first in order to apply and save attachment
    Set<Attachment> attachments = new HashSet<>();
    if(dto.getAttachment() != null) {
      String link = objectStorage.storeImage(Base64Util.extractBase64Data(dto.getAttachment()), ObjectStorage.ImageType.ATTACHMENT);
      Attachment attachment = new Attachment();
      attachment.setIdea(idea);
      attachment.setUrl(link);
      attachment = attachmentRepository.save(attachment);
      attachments.add(attachment);
    }
    idea.setAttachments(attachments);
    ideaRepository.save(idea);

    FetchIdeaDto fetchDto = idea.convertToDto(user);
    WebhookDataBuilder builder = new WebhookDataBuilder().withUser(user).withIdea(idea);
    idea.getBoard().getWebhookExecutor().executeWebhooks(Webhook.Event.IDEA_CREATE, builder.build());
    return fetchDto;
  }

  public FetchUserDto postUpvote(User user, Idea idea) {
    if(idea.getVoters().contains(user)) {
      throw new FeedbackyRestException(HttpStatus.BAD_REQUEST, "Idea with id " + idea.getId() + " is already upvoted by you.");
    }
    if(idea.getBoard().getSuspensedList().stream().anyMatch(suspended -> suspended.getUser().equals(user))) {
      throw new FeedbackyRestException(HttpStatus.BAD_REQUEST, "You've been suspended, please contact board owner for more information.");
    }
    Set<User> voters = idea.getVoters();
    voters.add(user);
    idea.setVoters(voters);
    ideaRepository.save(idea);
    //no need to expose
    return user.convertToDto().exposeSensitiveData(false);
  }

  public ResponseEntity deleteUpvote(User user, Idea idea) {
    if(!idea.getVoters().contains(user)) {
      throw new FeedbackyRestException(HttpStatus.BAD_REQUEST, "Idea with id " + idea.getId() + " is not upvoted by you.");
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

}
