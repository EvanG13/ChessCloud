package org.example.handlers.register;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.example.databases.MongoDBUtility;
import org.example.databases.UsersMongoDBUtility;
import org.example.entities.User;
import org.example.requestRecords.UserRequest;
import org.example.utils.EncryptPassword;

@AllArgsConstructor
public class RegisterService {
  private final UsersMongoDBUtility utility;

  public RegisterService() {
    this.utility = new UsersMongoDBUtility(MongoDBUtility.getInstance("users"));
  }

  public boolean doesEmailExist(String email) {
    Optional<User> user = utility.getByEmail(email);

    return user.isPresent();
  }

  public void registerUser(String email, String username, String password) {
    UserRequest newUser = new UserRequest(email, username, EncryptPassword.encrypt(password));

    utility.post(newUser);
  }
}
