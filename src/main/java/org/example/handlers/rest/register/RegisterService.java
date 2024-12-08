package org.example.handlers.rest.register;

import static com.mongodb.client.model.Filters.eq;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.example.entities.stats.Stats;
import org.example.entities.stats.StatsUtility;
import org.example.entities.user.User;
import org.example.entities.user.UserUtility;
import org.example.models.requests.RegisterRequest;
import org.example.utils.EncryptPassword;

@AllArgsConstructor
public class RegisterService {
  private final UserUtility userUtility;
  private final StatsUtility statsUtility;

  public RegisterService() {
    this.userUtility = new UserUtility();
    this.statsUtility = new StatsUtility();
  }

  public boolean doesEmailExist(String email) {
    Optional<User> user = userUtility.get(eq("email", email));

    return user.isPresent();
  }

  public boolean doesUsernameExist(String username) {
    Optional<User> user = userUtility.get(eq("username", username));

    return user.isPresent();
  }

  public void registerUser(RegisterRequest data) {
    User newUser =
        User.builder()
            .email(data.email())
            .password(EncryptPassword.encrypt(data.password()))
            .username(data.username())
            .build();
    userUtility.post(newUser);

    Stats newStats = new Stats(newUser.getId());
    statsUtility.post(newStats);
  }
}
