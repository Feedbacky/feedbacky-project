package net.feedbacky.app.service.idea;

import net.feedbacky.app.data.board.Board;
import net.feedbacky.app.data.idea.dto.FetchIdeaDto;
import net.feedbacky.app.data.idea.dto.PostIdeaDto;
import net.feedbacky.app.data.user.User;
import net.feedbacky.app.exception.types.ResourceNotFoundException;
import net.feedbacky.app.repository.UserRepository;
import net.feedbacky.app.repository.board.BoardRepository;
import net.feedbacky.app.util.PublicApiRequest;
import net.feedbacky.app.util.request.PublicRequestValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Plajer
 * <p>
 * Created at 13.08.2020
 */
@Service
public class PublicIdeaServiceImpl implements PublicIdeaService {

  private BoardRepository boardRepository;
  private IdeaPostCreator ideaPostCreator;
  private PublicRequestValidator publicRequestValidator;

  @Autowired
  public PublicIdeaServiceImpl(BoardRepository boardRepository, IdeaPostCreator ideaPostCreator, PublicRequestValidator publicRequestValidator) {
    this.boardRepository = boardRepository;
    this.ideaPostCreator = ideaPostCreator;
    this.publicRequestValidator = publicRequestValidator;
  }

  @Override
  public ResponseEntity<PublicApiRequest<FetchIdeaDto>> post(PostIdeaDto dto) {
    Board board = boardRepository.findByDiscriminator(dto.getDiscriminator())
            .orElseThrow(() -> new ResourceNotFoundException("Board with discriminator " + dto.getDiscriminator() + " not found."));
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    publicRequestValidator.validateApiKeyFromRequest(request, board);
    User user = publicRequestValidator.getUser(board, request);
    PublicApiRequest<FetchIdeaDto> data = new PublicApiRequest<>(user.getToken(), ideaPostCreator.post(dto, board, user));
    return ResponseEntity.status(HttpStatus.CREATED).body(data);
  }

}
