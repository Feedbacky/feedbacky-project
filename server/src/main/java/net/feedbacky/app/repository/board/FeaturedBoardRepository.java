package net.feedbacky.app.repository.board;

import net.feedbacky.app.data.board.featured.FeaturedBoard;

import com.cosium.spring.data.jpa.entity.graph.repository.EntityGraphPagingAndSortingRepository;

import org.springframework.stereotype.Repository;

/**
 * @author Plajer
 * <p>
 * Created at 16.06.2021
 */
@Repository
public interface FeaturedBoardRepository extends EntityGraphPagingAndSortingRepository<FeaturedBoard, Long> {
}
