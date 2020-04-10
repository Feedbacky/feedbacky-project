package net.feedbacky.app.service.board.social;

import net.feedbacky.app.data.board.dto.social.FetchSocialLinkDto;
import net.feedbacky.app.data.board.dto.social.PostSocialLinkDto;
import net.feedbacky.app.service.FeedbackyService;

import org.springframework.http.ResponseEntity;

import java.util.List;

/**
 * @author Plajer
 * <p>
 * Created at 23.12.2019
 */
public interface BoardSocialLinksService extends FeedbackyService {

  List<FetchSocialLinkDto> getAll(String discriminator);

  ResponseEntity<FetchSocialLinkDto> post(String discriminator, PostSocialLinkDto dto);

  ResponseEntity delete(long id);

}
