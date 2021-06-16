package net.feedbacky.app.data.board.dto.featured;

import lombok.Getter;
import net.feedbacky.app.data.FetchResponseDto;
import net.feedbacky.app.data.board.dto.FetchBoardDto;
import net.feedbacky.app.data.board.featured.FeaturedBoard;

/**
 * @author Plajer
 * <p>
 * Created at 16.06.2021
 */
@Getter
public class FetchFeaturedBoardDto implements FetchResponseDto<FetchFeaturedBoardDto, FeaturedBoard> {

  private FetchBoardDto board;
  private String description;

  @Override
  public FetchFeaturedBoardDto from(FeaturedBoard entity) {
    this.board = new FetchBoardDto().from(entity.getBoard());
    this.description = entity.getDescription();
    return this;
  }

}
