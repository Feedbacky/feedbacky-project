package net.feedbacky.app.util;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author Plajer
 * <p>
 * Created at 13.08.2020
 */
@AllArgsConstructor
@Data
public class PublicApiRequest<T> {

  private String userToken;
  private T data;

}
