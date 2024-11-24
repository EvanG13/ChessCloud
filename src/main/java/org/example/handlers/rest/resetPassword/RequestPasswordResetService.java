package org.example.handlers.rest.resetPassword;

import lombok.AllArgsConstructor;
import org.example.entities.token.PasswordResetToken;
import org.example.entities.user.User;
import org.example.entities.user.UserDbService;
import org.example.utils.MongoDBUtility;
import org.example.utils.ResendUtil;

import java.util.Calendar;
import java.util.Date;

@AllArgsConstructor
public class RequestPasswordResetService {
  private final MongoDBUtility<PasswordResetToken> passwordResetTokenDBUtility;
  private final UserDbService userDbService;

  public RequestPasswordResetService() {
    passwordResetTokenDBUtility = new MongoDBUtility<>("passwordResetToken", PasswordResetToken.class);
    userDbService = new UserDbService();
  }

  public void sendEmailIfRegistered(String to) {
    // Check user with email exists
    User user;
    try {
      user = userDbService.getByEmail(to);
    }
    catch (Exception e) {
      return;
    }

    // Set expiration time
    Calendar c = Calendar.getInstance();
    c.add(Calendar.HOUR, 2);
    Date expiration = c.getTime();

    // Create token
    PasswordResetToken passwordResetToken = PasswordResetToken.builder()
        .userId(user.getId())
        .expiresAt(expiration)
        .build();

    passwordResetTokenDBUtility.post(passwordResetToken);

    // Send email
    ResendUtil.sendPasswordResetEmail(to, passwordResetToken.getId());
  }
}