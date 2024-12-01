package org.example.entities.user;

import static com.mongodb.client.model.Filters.eq;

import java.util.Optional;
import org.example.exceptions.NotFound;
import org.example.utils.MongoDBUtility;

public class UserDbService {
  MongoDBUtility<User> mongoDBUtility;

  public UserDbService() {
    this.mongoDBUtility = new MongoDBUtility<>("users", User.class);
  }

  public User getByEmail(String email) throws NotFound {
    return mongoDBUtility
        .get(eq("email", email))
        .orElseThrow(() -> new NotFound("No user found with email " + email));
  }

  public User getByUsername(String username) throws NotFound {
    return mongoDBUtility
        .get(eq("username", username))
        .orElseThrow(() -> new NotFound("No user found with username " + username));
  }

  public void createUser(User user) {
    mongoDBUtility.post(user);
  }

  public boolean doesUserExist(String id) {
    Optional<User> optionalUser = mongoDBUtility.get(id);
    return optionalUser.isPresent();
  }

  public void deleteUser(String id) {
    mongoDBUtility.delete(id);
  }
}
