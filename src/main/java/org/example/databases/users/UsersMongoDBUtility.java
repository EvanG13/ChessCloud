package org.example.databases.users;

import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.example.databases.MongoDBUtility;
import org.example.entities.User;
import org.example.requestRecords.UserRequest;

public class UsersMongoDBUtility {
  private final MongoDBUtility utility;

  public UsersMongoDBUtility() {
    utility = MongoDBUtility.getInstance("users");
  }

  public UsersMongoDBUtility(MongoDBUtility utility) {
    this.utility = utility;
  }

  /**
   * Get a User by their id
   *
   * @param id id
   * @return user
   */
  public User get(String id) {
    Document doc = utility.get(id);
    if (doc == null) {
      return null;
    }

    return User.fromDocument(doc);
  }

  public User getByEmail(String email) {
    Document user = utility.get(Filters.eq("email", email));

    if (user == null) {
      return null;
    }

    return User.fromDocument(user);
  }

  public void deleteAllDocuments() {
    utility.delete();
  }

  /**
   * Create a new User
   *
   * @param userData user request data object
   */
  public void post(UserRequest userData) {
    utility.post(
        new Document("_id", new ObjectId())
            .append("email", userData.email())
            .append("password", userData.password())
            .append("username", userData.username()));
  }

  /**
   * Deletes a User by their id
   *
   * @param id id
   */
  public void delete(String id) {
    utility.delete(id);
  }

  /**
   * Update a user with the given id
   *
   * @param id object id
   * @param filter filter
   */
  public void patch(String id, Bson filter) {
    utility.patch(id, filter);
  }
}
