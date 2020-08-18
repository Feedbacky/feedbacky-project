package net.feedbacky.app.service.idea;

import net.feedbacky.app.data.board.Board;
import net.feedbacky.app.data.idea.Idea;
import net.feedbacky.app.data.idea.dto.FetchIdeaDto;
import net.feedbacky.app.data.idea.dto.PostIdeaDto;
import net.feedbacky.app.data.user.User;
import net.feedbacky.app.data.user.dto.FetchUserDto;
import net.feedbacky.app.exception.types.ResourceNotFoundException;
import net.feedbacky.app.repository.board.BoardRepository;
import net.feedbacky.app.repository.idea.IdeaRepository;
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
  private IdeaRepository ideaRepository;
  private IdeaPostUtils ideaPostCreator;
  private PublicRequestValidator publicRequestValidator;

  @Autowired
  public PublicIdeaServiceImpl(BoardRepository boardRepository, IdeaRepository ideaRepository, IdeaPostUtils ideaPostCreator, PublicRequestValidator publicRequestValidator) {
    this.boardRepository = boardRepository;
    this.ideaRepository = ideaRepository;
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

  @Override
  public PublicApiRequest<FetchUserDto> postUpvote(long id) {
    Idea idea = ideaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Idea with id " + id + " does not exist."));
    Board board = idea.getBoard();
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    publicRequestValidator.validateApiKeyFromRequest(request, board);
    User user = publicRequestValidator.getUser(board, request);
    return new PublicApiRequest<>(user.getToken(), ideaPostCreator.postUpvote(user, idea));
  }

  @Override
  public ResponseEntity<PublicApiRequest<?>> deleteUpvote(long id) {
    Idea idea = ideaRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Idea with id " + id + " does not exist."));
    Board board = idea.getBoard();
    HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
    publicRequestValidator.validateApiKeyFromRequest(request, board);
    User user = publicRequestValidator.getUser(board, request);
    return ResponseEntity.status(HttpStatus.NO_CONTENT).body(new PublicApiRequest<>(user.getToken(), ideaPostCreator.deleteUpvote(user, idea)));
  }
}
