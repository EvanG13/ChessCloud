package org.example.databases.users;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.example.databases.DynamoDBUtility;
import org.example.entities.User;
import org.example.requestRecords.UserRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.QueryRequest;

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
  public User get(String id) {
    Map<String, AttributeValue> item = utility.get(id);
    if (item == null) {
      return null;
    }

    return User.fromMap(item);
  }

  public User getByEmail(String email) {
    Map<String, AttributeValue> values = new HashMap<>();
    values.put(":emailVal", AttributeValue.builder().s(email).build());
    //    values.put(":passwordVal", AttributeValue.builder().s(password).build());

    QueryRequest queryRequest =
        QueryRequest.builder()
            .tableName("users")
            .indexName("emailPasswordIndex")
            .keyConditionExpression("email = :emailVal")
            .expressionAttributeValues(values)
            .build();

    Map<String, AttributeValue> item = utility.get(queryRequest);
    if (item == null) {
      return null;
    }

    return User.fromMap(item);
  }

  /**
   * Create a new User
   *
   * @param userData user request data object
   */
  public void post(UserRequest userData) {
    User duser =
        new User(
            UUID.randomUUID().toString(),
            userData.email(),
            userData.password(),
            userData.username());
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
