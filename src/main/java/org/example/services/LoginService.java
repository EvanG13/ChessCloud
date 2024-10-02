package org.example.services;

import static com.mongodb.client.model.Filters.eq;

import lombok.AllArgsConstructor;
import org.example.entities.user.User;
import org.example.exceptions.Unauthorized;
import org.example.utils.EncryptPassword;
import org.example.utils.MongoDBUtility;

@AllArgsConstructor
public class LoginService {

  private final MongoDBUtility<User> dbUtility;

  public LoginService() {
    this.dbUtility = new MongoDBUtility<>("users", User.class);
  }

  public User authenticateUser(String email, String plainTextPassword) throws Unauthorized {

    User user =
        dbUtility
            .get(eq("email", email))
            .orElseThrow(() -> new Unauthorized("Email or Password is incorrect"));

    String password = user.getPassword();
    if (!EncryptPassword.verify(plainTextPassword, password)) {
      throw new Unauthorized("Email or Password is incorrect");
    }

    return user;
  }
}
