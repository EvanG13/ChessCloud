package org.example.handlers.login;

import org.example.databases.DynamoDBUtility;
import org.example.databases.users.UsersDynamoDBUtility;
import org.example.entities.User;
import org.example.utils.EncryptPassword;

public class LoginService {

  private final UsersDynamoDBUtility dbUtility;

  public LoginService(UsersDynamoDBUtility dbUtility) {
    this.dbUtility = dbUtility;
  }

  public LoginService() {
    this.dbUtility = new UsersDynamoDBUtility(DynamoDBUtility.create("users", User.class));
  }

  public User authenticateUser(String email, String plainTextPassword) {

    User user = dbUtility.getByEmail(email);
    if (user == null) {
      return null;
    }

    if (!EncryptPassword.verify(plainTextPassword, user.getPassword())) {
      return null;
    }

    return user;
  }
}
