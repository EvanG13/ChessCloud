package org.example.databases;

import com.mongodb.client.model.Filters;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.example.entities.User;
import org.example.requestRecords.UserRequest;

@AllArgsConstructor
public class UsersMongoDBUtility {
  private final MongoDBUtility utility;

  public UsersMongoDBUtility() {
    utility = MongoDBUtility.getInstance("users");
  }

  public Optional<User> get(String id) {
    Document doc = utility.get(id);
    if (doc == null) {
      return Optional.empty();
    }

    return Optional.of(User.fromDocument(doc));
  }

  public Optional<User> getByEmail(String email) {
    Document user = utility.get(Filters.eq("email", email));
    if (user == null) {
      return Optional.empty();
    }

    return Optional.of(User.fromDocument(user));
  }

  public void post(UserRequest userData) {
    utility.post(
        new Document("_id", new ObjectId())
            .append("email", userData.email())
            .append("password", userData.password())
            .append("username", userData.username()));
  }

  public void delete(String id) {
    utility.delete(id);
  }

  public void patch(String id, Bson filter) {
    utility.patch(id, filter);
  }
}
