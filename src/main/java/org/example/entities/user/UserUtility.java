package org.example.entities.user;

import static com.mongodb.client.model.Filters.eq;

import org.example.exceptions.NotFound;
import org.example.utils.MongoDBUtility;

public class UserUtility extends MongoDBUtility<User> {
  public UserUtility() {
    super("users", User.class);
  }

  public UserUtility(String collection) {
    super(collection, User.class);
  }

  public User getByEmail(String email) throws NotFound {
    return get(eq("email", email))
        .orElseThrow(() -> new NotFound("No user found with email " + email));
  }

  public User getByUsername(String username) throws NotFound {
    return get(eq("username", username))
        .orElseThrow(() -> new NotFound("No user found with username " + username));
  }
}
