package org.example.handlers.login;

import org.example.databases.DynamoDBUtility;
import org.example.databases.users.UsersDynamoDBUtility;
import org.example.entities.User;
import org.example.utils.EncryptPassword;

public class LoginService {

  private final UsersDynamoDBUtility utility;

  public LoginService(UsersDynamoDBUtility dbUtility) {
    this.utility = dbUtility;
  }

  public LoginService() {
    this.utility = new UsersDynamoDBUtility(DynamoDBUtility.create("users"));
  }

  public User authenticateUser(String email, String plainTextPassword) {

    User user = utility.getByEmail(email);
    if (user == null) {
      return null;
    }

    if (!EncryptPassword.verify(plainTextPassword, user.getPassword())) {
      return null;
    }

    return user;
  }
}
