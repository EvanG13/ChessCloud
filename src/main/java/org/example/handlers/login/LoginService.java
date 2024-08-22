package org.example.handlers.login;

import java.util.Optional;
import org.example.databases.MongoDBUtility;
import org.example.databases.UsersMongoDBUtility;
import org.example.entities.User;
import org.example.utils.EncryptPassword;

public class LoginService {

  private final UsersMongoDBUtility dbUtility;

  public LoginService(UsersMongoDBUtility dbUtility) {
    this.dbUtility = dbUtility;
  }

  public LoginService() {
    this.dbUtility = new UsersMongoDBUtility(MongoDBUtility.getInstance("users"));
  }

  public Optional<User> authenticateUser(String email, String plainTextPassword) {

    Optional<User> optionalUser = dbUtility.getByEmail(email);
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
