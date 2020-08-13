package net.feedbacky.app.controller.idea;

import net.feedbacky.app.data.idea.dto.FetchIdeaDto;
import net.feedbacky.app.data.idea.dto.PostIdeaDto;
import net.feedbacky.app.service.idea.PublicIdeaService;
import net.feedbacky.app.util.PublicApiRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * @author Plajer
 * <p>
 * Created at 13.08.2020
 */
@RestController
@CrossOrigin
public class PublicIdeaRestController {

  private final PublicIdeaService publicIdeaService;

  @Autowired
  public PublicIdeaRestController(PublicIdeaService publicIdeaService) {
    this.publicIdeaService = publicIdeaService;
  }

  @PostMapping("v1/public/ideas/")
  public ResponseEntity<PublicApiRequest<FetchIdeaDto>> post(@Valid @RequestBody PostIdeaDto dto) {
    return publicIdeaService.post(dto);
  }

}
