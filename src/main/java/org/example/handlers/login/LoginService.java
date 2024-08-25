package org.example.handlers.login;

import com.mongodb.client.model.Filters;
import java.util.Optional;
import org.example.databases.MongoDBUtility;
import org.example.entities.User;
import org.example.utils.EncryptPassword;

public class LoginService {

  private final MongoDBUtility<User> dbUtility;

  public LoginService(MongoDBUtility<User> dbUtility) {
    this.dbUtility = dbUtility;
  }

  public LoginService() {
    this.dbUtility = new MongoDBUtility<>("users", User.class);
  }

  public Optional<User> authenticateUser(String email, String plainTextPassword) {

    Optional<User> optionalUser = dbUtility.get(Filters.eq("email", email));
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
