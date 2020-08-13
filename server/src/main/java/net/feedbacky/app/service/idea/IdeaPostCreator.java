package net.feedbacky.app.service.idea;

import net.feedbacky.app.data.board.Board;
import net.feedbacky.app.data.board.webhook.Webhook;
import net.feedbacky.app.data.board.webhook.WebhookDataBuilder;
import net.feedbacky.app.data.idea.Idea;
import net.feedbacky.app.data.idea.attachment.Attachment;
import net.feedbacky.app.data.idea.dto.FetchIdeaDto;
import net.feedbacky.app.data.idea.dto.PostIdeaDto;
import net.feedbacky.app.data.user.User;
import net.feedbacky.app.exception.FeedbackyRestException;
import net.feedbacky.app.repository.idea.AttachmentRepository;
import net.feedbacky.app.repository.idea.IdeaRepository;
import net.feedbacky.app.util.Base64Util;
import net.feedbacky.app.util.objectstorage.ObjectStorage;

import org.apache.commons.text.StringEscapeUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * @author Plajer
 * <p>
 * Created at 13.08.2020
 */
@Component
public class IdeaPostCreator {

  private IdeaRepository ideaRepository;
  private ObjectStorage objectStorage;
  private AttachmentRepository attachmentRepository;

  @Autowired
  public IdeaPostCreator(IdeaRepository ideaRepository, ObjectStorage objectStorage, AttachmentRepository attachmentRepository) {
    this.ideaRepository = ideaRepository;
    this.objectStorage = objectStorage;
    this.attachmentRepository = attachmentRepository;
  }

  public FetchIdeaDto post(PostIdeaDto dto, Board board, User user) {
    Optional<Idea> optional = ideaRepository.findByTitleAndBoard(dto.getTitle(), board);
    if(optional.isPresent() && optional.get().getBoard().getId().equals(board.getId())) {
      throw new FeedbackyRestException(HttpStatus.BAD_REQUEST, "Idea with that title in that board already exists.");
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

}
