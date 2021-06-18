package net.feedbacky.app.service.board.featured;

import net.feedbacky.app.data.board.dto.featured.FetchFeaturedBoardDto;
import net.feedbacky.app.data.board.featured.FeaturedBoard;
import net.feedbacky.app.repository.board.FeaturedBoardRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * @author Plajer
 * <p>
 * Created at 02.11.2019
 */
@Service
public class FeaturedBoardsServiceImpl implements FeaturedBoardsService {

  private List<Long> featuredBoards = new ArrayList<>();
  private FeaturedBoardRepository featuredBoardRepository;

  @Autowired
  public FeaturedBoardsServiceImpl(FeaturedBoardRepository featuredBoardRepository) {
    this.featuredBoardRepository = featuredBoardRepository;
  }

  @Scheduled(fixedDelay = 86_400_000)
  public void scheduleFeaturedBoardsSelectionTask() {
    featuredBoards.clear();
    Iterable<FeaturedBoard> iterable = featuredBoardRepository.findAll();
    List<FeaturedBoard> boards = new ArrayList<>();
    iterable.forEach(boards::add);
    Collections.shuffle(boards);
    int i = 2;
    for(FeaturedBoard board : boards) {
      featuredBoards.add(board.getId());
      i--;
      if(i <= 0) {
        return;
      }
    }
  }

  @Override
  public List<FetchFeaturedBoardDto> getAll() {
    List<FetchFeaturedBoardDto> boards = new ArrayList<>();
    for(Long id : featuredBoards) {
      Optional<FeaturedBoard> board = featuredBoardRepository.findById(id);
      //someone deleted board, reschedule and load again.
      if(!board.isPresent()) {
        scheduleFeaturedBoardsSelectionTask();
        return getAll();
      }
      boards.add(new FetchFeaturedBoardDto().from(board.get()));
    }
    return boards;
  }
}
