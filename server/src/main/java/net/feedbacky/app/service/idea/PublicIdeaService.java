package net.feedbacky.app.service.idea;

import net.feedbacky.app.data.idea.dto.FetchIdeaDto;
import net.feedbacky.app.data.idea.dto.PostIdeaDto;
import net.feedbacky.app.data.user.dto.FetchUserDto;
import net.feedbacky.app.service.FeedbackyService;
import net.feedbacky.app.util.PublicApiRequest;

import org.springframework.http.ResponseEntity;

/**
 * @author Plajer
 * <p>
 * Created at 13.08.2020
 */
public interface PublicIdeaService extends FeedbackyService {

  ResponseEntity<PublicApiRequest<FetchIdeaDto>> post(PostIdeaDto dto);

  PublicApiRequest<FetchUserDto> postUpvote(long id);

  ResponseEntity<PublicApiRequest<?>> deleteUpvote(long id);

}
