package net.feedbacky.app.data.idea;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.feedbacky.app.data.board.Board;
import net.feedbacky.app.data.idea.attachment.Attachment;
import net.feedbacky.app.data.idea.comment.Comment;
import net.feedbacky.app.data.idea.dto.FetchIdeaDto;
import net.feedbacky.app.data.tag.Tag;
import net.feedbacky.app.data.tag.dto.FetchTagDto;
import net.feedbacky.app.data.user.User;
import net.feedbacky.app.util.mailservice.MailService;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.CreationTimestamp;
import org.modelmapper.ModelMapper;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Plajer
 * <p>
 * Created at 30.09.2019
 */
@Entity
@Table(name = "ideas")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class Idea implements Serializable {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  private Board board;
  private String title;
  @Column(name = "description", columnDefinition = "text", length = 65_535)
  private String description;
  @ManyToOne
  private User creator;
  @ManyToMany(fetch = FetchType.EAGER)
  private Set<User> voters = new HashSet<>();
  //always the same as voters.size()
  private int votersAmount;
  private double trendScore;
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "idea")
  private Set<Comment> comments = new HashSet<>();
  @ManyToMany(fetch = FetchType.LAZY)
  private Set<Tag> tags = new HashSet<>();
  @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "idea")
  private Set<Attachment> attachments = new HashSet<>();
  @ManyToMany(fetch = FetchType.EAGER)
  private Set<User> subscribers = new HashSet<>();
  private IdeaStatus status;
  @CreationTimestamp
  private Date creationDate;
  private boolean edited;

  public void setVoters(Set<User> voters) {
    this.voters = voters;
    this.votersAmount = voters.size();
    this.trendScore = getCalculatedTrendScore();
  }

  public double getCalculatedTrendScore() {
    return ((double) voters.size() - 1.0) / Math.pow(ChronoUnit.DAYS.between(creationDate.toInstant(), Instant.now()) + 2.0, 1.8);
  }

  public String toViewLink() {
    String slug = title;
    slug = slug.replaceAll("[\\W_]", "-")
            .replaceAll("(\\W)\\1+", "-")
            .replaceAll("^(-)", "")
            .replaceAll("(-)$", "");
    return MailService.HOST_ADDRESS + "/i/" + slug + "." + id;
  }

  public FetchIdeaDto convertToDto(User user) {
    FetchIdeaDto dto = new ModelMapper().map(this, FetchIdeaDto.class);
    dto.setOpen(status == IdeaStatus.OPENED);
    Set<FetchTagDto> tagDtos = new HashSet<>();
    for(Tag tag : tags) {
      tagDtos.add(tag.convertToDto());
    }
    dto.setAttachments(attachments.stream().map(Attachment::convertToDto).collect(Collectors.toList()));
    dto.setBoardDiscriminator(board.getDiscriminator());
    dto.setVotersAmount(voters.size());
    //count only public and non special comments
    dto.setCommentsAmount(comments.stream().filter(comment -> !comment.isSpecial()).filter(comment -> comment.getViewType() == Comment.ViewType.PUBLIC).count());
    dto.setTags(tagDtos);
    dto.setUpvoted(voters.contains(user));
    dto.setSubscribed(subscribers.contains(user));
    dto.setUser(creator.convertToDto().exposeSensitiveData(false).convertToSimpleDto());
    return dto;
  }

  public enum IdeaStatus {
    OPENED(true), CLOSED(false);

    private final boolean value;

    IdeaStatus(boolean value) {
      this.value = value;
    }

    public static IdeaStatus toIdeaStatus(boolean state) {
      if(state) {
        return OPENED;
      }
      return CLOSED;
    }

    public boolean getValue() {
      return value;
    }

  }

}
