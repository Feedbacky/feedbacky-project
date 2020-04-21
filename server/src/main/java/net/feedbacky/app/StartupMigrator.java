package net.feedbacky.app;

import net.feedbacky.app.data.idea.Idea;
import net.feedbacky.app.repository.idea.IdeaRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Plajer
 * <p>
 * Created at 21.04.2020
 */
@Component
public class StartupMigrator {

  private IdeaRepository ideaRepository;

  @Autowired
  public StartupMigrator(IdeaRepository ideaRepository) {
    this.ideaRepository = ideaRepository;
  }

  @EventListener(ApplicationStartedEvent.class)
  public void attemptMigration() {
    int i = 0;
    for(Idea idea : ideaRepository.findByTrendScoreEquals(0)) {
      idea.setTrendScore(idea.getCalculatedTrendScore());
      ideaRepository.save(idea);
      i++;
    }
    Logger.getLogger("Migrator").log(Level.INFO, "Migrated " + i + " ideas to use Trend Score [from 0.1.0-beta]");
  }

}
