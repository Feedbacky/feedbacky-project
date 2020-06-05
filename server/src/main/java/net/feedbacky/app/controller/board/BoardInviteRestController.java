package net.feedbacky.app.controller.board;

import net.feedbacky.app.data.board.dto.FetchBoardDto;
import net.feedbacky.app.data.board.dto.invite.FetchInviteDto;
import net.feedbacky.app.data.board.dto.invite.PostInviteDto;
import net.feedbacky.app.data.user.dto.FetchSimpleUserDto;
import net.feedbacky.app.service.board.invite.BoardInviteService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

import java.util.List;

/**
 * @author Plajer
 * <p>
 * Created at 29.11.2019
 */
@CrossOrigin
@RestController
public class BoardInviteRestController {

  private final BoardInviteService boardInviteService;

  @Autowired
  public BoardInviteRestController(BoardInviteService boardInviteService) {
    this.boardInviteService = boardInviteService;
  }

  @GetMapping("v1/boards/{discriminator}/invitedUsers")
  public List<FetchSimpleUserDto> getAllInvited(@PathVariable String discriminator) {
    return boardInviteService.getAllInvited(discriminator);
  }

  @GetMapping("v1/boards/{discriminator}/invitations")
  public List<FetchInviteDto> getAll(@PathVariable String discriminator) {
    return boardInviteService.getAll(discriminator);
  }

  @PostMapping("v1/invitations/{code}/accept")
  public FetchBoardDto postAccept(@PathVariable String code) {
    return boardInviteService.postAccept(code);
  }

  @PostMapping("v1/boards/{discriminator}/invitations")
  public ResponseEntity<FetchInviteDto> post(@PathVariable String discriminator, @Valid @RequestBody PostInviteDto postInviteDto) {
    return boardInviteService.post(discriminator, postInviteDto);
  }

  @DeleteMapping("v1/invitations/{id}")
  public ResponseEntity delete(@PathVariable long id) {
    return boardInviteService.delete(id);
  }

  @DeleteMapping("v1/boards/{discriminator}/invitedUsers/{id}")
  public ResponseEntity deleteInvited(@PathVariable String discriminator, @PathVariable long id) {
    return boardInviteService.deleteInvited(discriminator, id);
  }

}
