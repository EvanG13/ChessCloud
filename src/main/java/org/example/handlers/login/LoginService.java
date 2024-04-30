package org.example.handlers.login;

import org.example.databases.MongoDBUtility;
import org.example.databases.users.UsersMongoDBUtility;
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
