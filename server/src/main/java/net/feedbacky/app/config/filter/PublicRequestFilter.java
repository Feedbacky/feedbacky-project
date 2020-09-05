package net.feedbacky.app.config.filter;

import net.feedbacky.app.exception.FeedbackyRestException;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * @author Plajer
 * <p>
 * Created at 13.08.2020
 */
@Component
public class PublicRequestFilter extends OncePerRequestFilter {

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getServletPath();
    return !path.startsWith("/v1/public/");
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
    String tokenHeader = request.getHeader("Authorization");

    response.setContentType("application/json");
    if(tokenHeader == null) {
      throw new FeedbackyRestException(HttpStatus.BAD_REQUEST, "Authorization header required to use Public API.");
    }
    if(!tokenHeader.startsWith("Apikey ")) {
      throw new FeedbackyRestException(HttpStatus.BAD_REQUEST, "Only Apikeys supported in Public API.");
    }
    chain.doFilter(request, response);
  }

}
