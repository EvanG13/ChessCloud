package org.example.handlers.rest.resetPassword;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import lombok.AllArgsConstructor;
import org.example.entities.connection.Connection;
import org.example.entities.session.Session;
import org.example.entities.token.PasswordResetToken;
import org.example.entities.user.User;
import org.example.exceptions.BadRequest;
import org.example.exceptions.InternalServerError;
import org.example.exceptions.NotFound;
import org.example.utils.EncryptPassword;
import org.example.utils.MongoDBUtility;

@AllArgsConstructor
public class ResetPasswordService {
  private final MongoDBUtility<User> userDBUtility;
  private final MongoDBUtility<PasswordResetToken> passwordResetTokenDBUtility;
  private final MongoDBUtility<Connection> connectionDBUtility;
  private final MongoDBUtility<Session> sessionDBUtility;

  public ResetPasswordService() {
    userDBUtility = new MongoDBUtility<>("users", User.class);
    passwordResetTokenDBUtility = new MongoDBUtility<>("passwordResetToken", PasswordResetToken.class);
    connectionDBUtility = new MongoDBUtility<>("connections", Connection.class);
    sessionDBUtility = new MongoDBUtility<>("sessions", Session.class);
  }

  public void resetPassword(String token, String email, String newPassword) throws NotFound, BadRequest, InternalServerError {
    // Check token exists
    PasswordResetToken passwordResetToken = passwordResetTokenDBUtility
        .get(token)
        .orElseThrow(() -> new NotFound("Invalid token"));

    // Ensure user tied to token exists
    User user = userDBUtility
        .get(passwordResetToken.getUserId())
        .orElseThrow(() -> new InternalServerError("User doesn't exist with id: " + passwordResetToken.getUserId()));

    // Check email matches
    if (user.getEmail().equals(email))
      throw new BadRequest("Email not paired with token");


    // Check token not expired
    if (passwordResetToken.isExpired()) {
      passwordResetTokenDBUtility.delete(token);
      throw new InternalServerError("Token expired");
    }

    // <Password requirement validation here>

    // Hash and update password
    userDBUtility.patch(user.getId(), Updates.set("password", EncryptPassword.encrypt(newPassword)));

    // Delete token
    passwordResetTokenDBUtility.delete(token);

    // Sign out everywhere
    // Delete User's stored sessions
    sessionDBUtility.deleteMany(Filters.eq("userId", user.getId()));

    // Delete User's stored connections
    connectionDBUtility.deleteMany(Filters.eq("username", user.getUsername()));
  }
}
