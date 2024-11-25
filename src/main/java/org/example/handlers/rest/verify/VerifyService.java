package org.example.handlers.rest.verify;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import lombok.AllArgsConstructor;
import org.example.entities.token.BaseToken;
import org.example.entities.user.User;
import org.example.entities.token.VerificationToken;
import org.example.exceptions.InternalServerError;
import org.example.exceptions.NotFound;
import org.example.utils.MongoDBUtility;

@AllArgsConstructor
public class VerifyService {
  private final MongoDBUtility<User> userDBUtility;
  private final MongoDBUtility<VerificationToken> verificationTokenDBUtility;

  public VerifyService() {
    userDBUtility = new MongoDBUtility<>("users", User.class);
    verificationTokenDBUtility = new MongoDBUtility<>("verificationToken", VerificationToken.class);
  }

  public void verify(String token) throws NotFound, InternalServerError {
    String hashedToken = BaseToken.hashToken(token);

    // Check token exists
    VerificationToken verificationToken = verificationTokenDBUtility
        .get(Filters.eq("token", hashedToken))
        .orElseThrow(() -> new NotFound("Invalid token"));

    // Ensure user tied to token exists
    User user = userDBUtility
        .get(verificationToken.getUserId())
        .orElseThrow(() -> new InternalServerError("User doesn't exist with id: " + verificationToken.getUserId()));

    // If user already verified, delete token
    if (user.getVerified()) {
      verificationTokenDBUtility.delete(token);
      throw new InternalServerError("User already verified");
    }

    // Check token not expired
    if (verificationToken.isExpired()) {
      verificationTokenDBUtility.delete(token);
      throw new InternalServerError("Token expired");
    }

    // Token passes all checks
    // Update user as verified
    userDBUtility.patch(user.getId(), Updates.set("verified", true));

    // Delete token
    verificationTokenDBUtility.delete(token);
  }
}
