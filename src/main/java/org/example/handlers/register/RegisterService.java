package org.example.handlers.register;

import com.mongodb.client.model.Filters;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.example.databases.MongoDBUtility;
import org.example.entities.User;
import org.example.requestRecords.RegisterRequest;
import org.example.utils.EncryptPassword;

@AllArgsConstructor
public class RegisterService {
  private final MongoDBUtility<User> utility;

  public RegisterService() {
    this.utility = new MongoDBUtility<>("users", User.class);
  }

  public boolean doesEmailExist(String email) {
    Optional<User> user = utility.get(Filters.eq("email", email));

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

    utility.post(newUser);
  }
}
