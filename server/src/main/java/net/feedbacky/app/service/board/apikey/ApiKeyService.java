package net.feedbacky.app.service.board.apikey;

import net.feedbacky.app.data.board.dto.FetchBoardDto;
import net.feedbacky.app.service.FeedbackyService;

import org.springframework.http.ResponseEntity;

/**
 * @author Plajer
 * <p>
 * Created at 13.08.2020
 */
public interface ApiKeyService extends FeedbackyService {

  ResponseEntity<FetchBoardDto> patch(String discriminator);

  ResponseEntity delete(String discriminator);

}
