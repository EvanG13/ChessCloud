package org.example.handlers.login;

import static com.mongodb.client.model.Filters.eq;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.example.databases.MongoDBUtility;
import org.example.entities.User;
import org.example.utils.EncryptPassword;

@AllArgsConstructor
public class LoginService {

  private final MongoDBUtility<User> dbUtility;

  public LoginService() {
    this.dbUtility = new MongoDBUtility<>("users", User.class);
  }

  public Optional<User> authenticateUser(String email, String plainTextPassword) {

    Optional<User> optionalUser = dbUtility.get(eq("email", email));
    if (optionalUser.isEmpty()) {
      return Optional.empty();
    }

    String password = optionalUser.get().getPassword();
    if (!EncryptPassword.verify(plainTextPassword, password)) {
      return Optional.empty();
    }

    return optionalUser;
  }
}
