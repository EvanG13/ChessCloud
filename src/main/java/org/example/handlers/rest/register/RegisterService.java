package org.example.handlers.rest.register;

import static com.mongodb.client.model.Filters.eq;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.example.entities.stats.Stats;
import org.example.entities.user.User;
import org.example.entities.token.VerificationToken;
import org.example.models.requests.RegisterRequest;
import org.example.utils.EncryptPassword;
import org.example.utils.MongoDBUtility;
import org.example.utils.ResendUtil;

@AllArgsConstructor
public class RegisterService {
  private final MongoDBUtility<User> userDBUtility;
  private final MongoDBUtility<Stats> statsDBUtility;
  private final MongoDBUtility<VerificationToken> verificationTokenDBUtility;

  public RegisterService() {
    this.userDBUtility = new MongoDBUtility<>("users", User.class);
    this.statsDBUtility = new MongoDBUtility<>("stats", Stats.class);
    this.verificationTokenDBUtility = new MongoDBUtility<>("verificationToken", VerificationToken.class);
  }

  public boolean doesEmailExist(String email) {
    Optional<User> user = userDBUtility.get(eq("email", email));

    return user.isPresent();
  }

  public boolean doesUsernameExist(String username) {
    Optional<User> user = userDBUtility.get(eq("username", username));

    return user.isPresent();
  }

  public void registerUser(RegisterRequest data) {
    // Setup user
    User newUser =
        User.builder()
            .email(data.email())
            .password(EncryptPassword.encrypt(data.password()))
            .username(data.username())
            .verified(false)
            .build();
    userDBUtility.post(newUser);

    // Setup user's stats
    Stats newStats = new Stats(newUser.getId());
    statsDBUtility.post(newStats);

    // Setup Verification requirement
    // Set expiration time
    Calendar c = Calendar.getInstance();
    c.add(Calendar.HOUR, 2);
    Date expiration = c.getTime();

    // Create token
    VerificationToken verificationToken = VerificationToken.builder()
        .userId(newUser.getId())
        .expiresAt(expiration)
        .build();
    verificationTokenDBUtility.post(verificationToken);

    // Send email
    ResendUtil.sendVerificationEmail(data.email(), verificationToken.getId());
  }
}
