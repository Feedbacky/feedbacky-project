package net.feedbacky.app.service.board.featured;

import net.feedbacky.app.data.board.dto.featured.FetchFeaturedBoardDto;

import java.util.List;

/**
 * @author Plajer
 * <p>
 * Created at 02.11.2019
 */
public interface FeaturedBoardsService {

  List<FetchFeaturedBoardDto> getAll();

}
