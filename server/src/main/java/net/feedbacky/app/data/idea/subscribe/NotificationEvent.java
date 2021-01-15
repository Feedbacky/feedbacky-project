package net.feedbacky.app.data.idea.subscribe;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Plajer
 * <p>
 * Created at 12.01.2021
 */
@Data
@AllArgsConstructor
public class NotificationEvent {

  private SubscriptionExecutor.Event eventType;
  private Serializable object;
  private String content;

}
