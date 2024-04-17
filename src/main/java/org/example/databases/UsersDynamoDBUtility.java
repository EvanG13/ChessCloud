package org.example.databases;

import java.util.Map;
import java.util.UUID;
import org.example.entities.DynamoUser;
import org.example.requestRecords.UserRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class UsersDynamoDBUtility {
  private final DynamoDBUtility utility;

  public UsersDynamoDBUtility(DynamoDBUtility utility) {
    this.utility = utility;
  }

  /**
   * Get a User by their id
   *
   * @param id id
   * @return user
   */
  public DynamoUser get(String id) {
    Map<String, AttributeValue> item = utility.get(id);
    if (item == null) {
      return null;
    }

    return DynamoUser.fromMap(item);
  }

  /**
   * Create a new User
   *
   * @param userData user request data object
   */
  public void post(UserRequest userData) {
    DynamoUser duser =
        new DynamoUser(
            UUID.randomUUID().toString(),
            userData.email(),
            userData.username(),
            userData.password());
    Map<String, AttributeValue> userMap = duser.toMap();

    utility.post(userMap);
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
  //    public void patch(String id, Bson filter) {
  //        utility.patch(id, filter);
  //    }
}
