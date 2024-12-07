package org.example.handlers.rest.login;

import static com.mongodb.client.model.Filters.eq;

import lombok.AllArgsConstructor;
import org.example.entities.user.User;
import org.example.entities.user.UserUtility;
import org.example.exceptions.Unauthorized;
import org.example.utils.EncryptPassword;

@AllArgsConstructor
public class LoginService {
  private final UserUtility userUtility;

  public LoginService() {
    this.userUtility = new UserUtility();
  }

  public User authenticateUser(String email, String plainTextPassword) throws Unauthorized {
    User user =
        userUtility
            .get(eq("email", email))
            .orElseThrow(() -> new Unauthorized("Email or Password is incorrect"));

    String password = user.getPassword();
    if (!EncryptPassword.verify(plainTextPassword, password))
      throw new Unauthorized("Email or Password is incorrect");

    return user;
  }
}
