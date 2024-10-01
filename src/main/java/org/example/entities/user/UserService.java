package org.example.entities.user;

import org.example.exceptions.NotFound;
import org.example.utils.MongoDBUtility;

import static com.mongodb.client.model.Filters.eq;

public class UserService {
  MongoDBUtility<User> mongoDBUtility;

  public UserService() {
    this.mongoDBUtility = new MongoDBUtility<>("users", User.class);
  }

  public User getByEmail(String email) throws NotFound {
    return mongoDBUtility
        .get(eq("email", email))
        .orElseThrow(() -> new NotFound("No user found with email " + email));
  }

  public void createUser(User user) {
    mongoDBUtility.post(user);
  }

  public void deleteUser(String id) {
    mongoDBUtility.delete(id);
  }
}
