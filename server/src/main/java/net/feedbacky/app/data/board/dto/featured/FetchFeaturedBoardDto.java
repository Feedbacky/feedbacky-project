package net.feedbacky.app.data.board.dto.featured;

import lombok.Getter;
import net.feedbacky.app.data.FetchResponseDto;
import net.feedbacky.app.data.board.dto.FetchBoardDto;
import net.feedbacky.app.data.board.featured.FeaturedBoard;
import net.feedbacky.app.data.user.dto.FetchSimpleUserDto;

/**
 * @author Plajer
 * <p>
 * Created at 16.06.2021
 */
@Getter
public class FetchFeaturedBoardDto implements FetchResponseDto<FetchFeaturedBoardDto, FeaturedBoard> {

  private FetchBoardDto board;
  private FetchSimpleUserDto creatorDetails;
  private String usage;
  private String description;

  @Override
  public FetchFeaturedBoardDto from(FeaturedBoard entity) {
    this.board = new FetchBoardDto().from(entity.getBoard());
    this.creatorDetails = new FetchSimpleUserDto().from(entity.getBoard().getCreator());
    this.usage = entity.getUsage();
    this.description = entity.getDescription();
    return this;
  }

}
