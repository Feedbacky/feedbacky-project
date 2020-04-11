package net.feedbacky.app.service.user;

import net.feedbacky.app.config.UserAuthenticationToken;
import net.feedbacky.app.exception.types.InvalidAuthenticationException;
import net.feedbacky.app.exception.types.ResourceNotFoundException;
import net.feedbacky.app.repository.UserRepository;
import net.feedbacky.app.data.board.dto.moderator.FetchUserPermissionDto;
import net.feedbacky.app.data.board.moderator.Moderator;
import net.feedbacky.app.data.user.ConnectedAccount;
import net.feedbacky.app.data.user.User;
import net.feedbacky.app.data.user.dto.FetchConnectedAccount;
import net.feedbacky.app.data.user.dto.FetchUserDto;
import net.feedbacky.app.data.user.dto.PatchUserDto;
import net.feedbacky.app.service.ServiceUser;
import net.feedbacky.app.util.RequestValidator;
import net.feedbacky.app.util.mailservice.MailHandler;
import net.feedbacky.app.util.mailservice.MailPlaceholderParser;
import net.feedbacky.app.util.mailservice.MailService;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.apache.commons.lang3.RandomStringUtils;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Plajer
 * <p>
 * Created at 10.10.2019
 */
@Service
public class UserServiceImpl implements UserService {

  private UserRepository userRepository;
  private MailHandler mailHandler;

  @Autowired
  public UserServiceImpl(UserRepository userRepository, MailHandler mailHandler) {
    this.userRepository = userRepository;
    this.mailHandler = mailHandler;
  }

  @Override
  public FetchUserDto getSelf() {
    UserAuthenticationToken auth = RequestValidator.getContextAuthentication();
    User user = userRepository.findByEmail(((ServiceUser) auth.getPrincipal()).getEmail())
            .orElseThrow(() -> new InvalidAuthenticationException("User session not found. Try again with new token"));
    return user.convertToDto().exposeSensitiveData(true);
  }

  @Override
  public List<FetchConnectedAccount> getSelfConnectedAccounts() {
    UserAuthenticationToken auth = RequestValidator.getContextAuthentication();
    User user = userRepository.findByEmail(((ServiceUser) auth.getPrincipal()).getEmail())
            .orElseThrow(() -> new InvalidAuthenticationException("User session not found. Try again with new token"));
    return user.getConnectedAccounts().stream().map(ConnectedAccount::convertToDto).collect(Collectors.toList());
  }

  @Override
  public List<FetchUserPermissionDto> getSelfPermissions() {
    UserAuthenticationToken auth = RequestValidator.getContextAuthentication();
    User user = userRepository.findByEmail(((ServiceUser) auth.getPrincipal()).getEmail())
            .orElseThrow(() -> new InvalidAuthenticationException("User session not found. Try again with new token"));
    return user.getPermissions().stream().map(Moderator::convertToUserPermissionDto).collect(Collectors.toList());
  }

  @Override
  public List<FetchUserPermissionDto> getPermissions(long id) {
    User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User does not exist of id " + id));
    return user.getPermissions().stream().map(Moderator::convertToUserPermissionDto).collect(Collectors.toList());
  }

  @Override
  public FetchUserDto get(long id) {
    User user = userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User does not exist of id " + id));
    Map<String, Object> data = new ObjectMapper().convertValue(user.convertToDto().exposeSensitiveData(false), Map.class);
    return new ObjectMapper().convertValue(data, FetchUserDto.class);
  }

  @Override
  public FetchUserDto patchSelf(PatchUserDto dto) {
    UserAuthenticationToken auth = RequestValidator.getContextAuthentication();
    User user = userRepository.findByEmail(((ServiceUser) auth.getPrincipal()).getEmail())
            .orElseThrow(() -> new InvalidAuthenticationException("User session not found. Try again with new token"));

    ModelMapper mapper = new ModelMapper();
    mapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
    mapper.map(dto, user);
    userRepository.save(user);
    return user.convertToDto().exposeSensitiveData(true);
  }

  @Override
  public ResponseEntity deactivateSelf() {
    UserAuthenticationToken auth = RequestValidator.getContextAuthentication();
    User user = userRepository.findByEmail(((ServiceUser) auth.getPrincipal()).getEmail())
            .orElseThrow(() -> new InvalidAuthenticationException("User session not found. Try again with new token"));
    //better to run sync now
    MailService.EmailTemplate template = MailService.EmailTemplate.ACCOUNT_DEACTIVATED;
    String subject = MailPlaceholderParser.parseAllAvailablePlaceholders(template.getSubject(), template, null, user, null);
    String text = MailPlaceholderParser.parseAllAvailablePlaceholders(template.getLegacyText(), template, null, user, null);
    String html = MailPlaceholderParser.parseAllAvailablePlaceholders(template.getHtml(), template, null, user, null);
    mailHandler.getMailService().send(user.getEmail(), subject, text, html);
    user.setEmail("deactivated-" + RandomStringUtils.randomAlphanumeric(6) + "@feedbacky.net");
    user.setAvatar("https://cdn.feedbacky.net/static/img/default_avatar.png");
    user.setUsername("Anonymous");
    user.setConnectedAccounts(new HashSet<>());
    userRepository.save(user);
    return ResponseEntity.noContent().build();
  }
}
