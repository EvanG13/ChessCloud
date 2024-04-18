package org.example.handlers.login;

import org.example.databases.DynamoDBUtility;
import org.example.databases.users.UsersDynamoDBUtility;
import org.example.entities.User;

public class LoginService {

  private final String email;
  private final String password;

  public LoginService(String email, String password) {
    this.email = email;
    this.password = password;
  }

  public boolean authenticateUser() {
    UsersDynamoDBUtility ddb = new UsersDynamoDBUtility(DynamoDBUtility.create("users"));

    User user = ddb.getByEmailAndPassword(email, password);

    return user != null;
  }
}
