package org.example.services;

import static com.mongodb.client.model.Filters.eq;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.example.entities.Stats;
import org.example.entities.User;
import org.example.models.requests.RegisterRequest;
import org.example.utils.EncryptPassword;
import org.example.utils.MongoDBUtility;

@AllArgsConstructor
public class RegisterService {
  private final MongoDBUtility<User> userDBUtility;
  private final MongoDBUtility<Stats> statsDBUtility;

  public RegisterService() {
    this.userDBUtility = new MongoDBUtility<>("users", User.class);
    this.statsDBUtility = new MongoDBUtility<>("stats", Stats.class);
  }

  public boolean doesEmailExist(String email) {
    Optional<User> user = userDBUtility.get(eq("email", email));

    return user.isPresent();
  }

  public void registerUser(RegisterRequest data) {
    User newUser =
        User.builder()
            .id(new ObjectId().toString())
            .email(data.email())
            .password(EncryptPassword.encrypt(data.password()))
            .username(data.username())
            .build();
    userDBUtility.post(newUser);

    Stats newStats = new Stats(newUser.getId());
    statsDBUtility.post(newStats);
  }
}
