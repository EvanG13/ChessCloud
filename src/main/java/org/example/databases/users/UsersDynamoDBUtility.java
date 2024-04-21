package org.example.databases.users;

import java.util.UUID;
import org.example.databases.DynamoDBUtility;
import org.example.entities.User;
import org.example.requestRecords.UserRequest;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class UsersDynamoDBUtility {

  private final DynamoDBUtility<User> dbUtility;

  public UsersDynamoDBUtility(DynamoDBUtility<User> dbUtility) {
    this.dbUtility = dbUtility;
  }

  /**
   * Get a User by their id
   *
   * @param id id
   * @return user
   */
  public User get(String id) {
    return dbUtility.get(id);
  }

  public User getByEmail(String email) {
    QueryConditional queryConditional =
        QueryConditional.keyEqualTo(
            Key.builder().partitionValue(AttributeValue.builder().s(email).build()).build());

    final String INDEX_NAME = "emailPasswordIndex";

    return dbUtility.get(queryConditional, INDEX_NAME);
  }

  /**
   * Create a new User
   *
   * @param userData user request data object
   */
  public void post(UserRequest userData) {
    User user =
        new User(
            UUID.randomUUID().toString(),
            userData.email(),
            userData.password(),
            userData.username());

    dbUtility.post(user);
  }

  /**
   * Deletes a User by their id
   *
   * @param id id
   */
  public void delete(String id) {
    dbUtility.delete(id);
  }

  /**
   * Patches a given user. The passed in user data must at MINIMUM include the user's primary key
   * (id) All other attributes are optional. Only the id and the attributes you want update must be
   * included
   *
   * @param userdata user data
   */
  public void patch(User userdata) throws IllegalAccessException {
    if (userdata.getId() == null) {
      throw new IllegalAccessException("Valid User id must be included within the user");
    }

    dbUtility.patch(userdata);
  }
}
