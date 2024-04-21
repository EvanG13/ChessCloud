package org.example.databases;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.example.entities.User;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Tag;
import software.amazon.awssdk.enhanced.dynamodb.Expression;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.*;

@Tag("Integration")
public class DynamoDBUtilityTest {

  static DynamoDBUtility<User> utility = DynamoDBUtility.create("users", User.class);

  static User user;
  static final String userId = "194e4010-d49b-496e-bed8-b96c713e2110";
  static final String email = "cleve@gmail.com";
  static final String password = "1234";
  static final String username = "cleve";

  static final String listUser1Id = "e3c9ef65-a29c-4366-b54d-2c89d9e4ffdf";
  static User listUser1;

  static final String listUser2Id = "ff3c1a53-fade-4328-82ad-66ec7f89fde8";
  static User listUser2;

  @BeforeAll
  public static void setUp() {
    user = new User(userId, email, password, username);
    utility.post(user);

    listUser1 = new User(listUser1Id, "listuser1@gmail.com", "matching", "alsodoesnt");
    listUser2 = new User(listUser2Id, "listuser1@gmail.com", "matching", "alsodoesnt");

    utility.post(listUser1);
    utility.post(listUser2);
  }

  @AfterAll
  public static void tearDown() {
    utility.delete(userId);
    utility.delete(listUser1Id);
    utility.delete(listUser2Id);
  }

  @DisplayName("Can get an item from dynamoDB \uD83E\uDD8D")
  @Test
  public void getItemById() {
    try {
      User actual = utility.get(userId);
      assertNotNull(actual);

      assertEquals(userId, actual.getId());
      assertEquals(email, actual.getEmail());
      assertEquals(password, actual.getPassword());
      assertEquals(username, actual.getUsername());
    } catch (DynamoDbException e) {
      e.printStackTrace();
      fail("fail");
    }
  }

  @DisplayName("Can get an item from dynamoDB by querying \uD83E\uDD8D")
  @Test
  public void getItemByQuery() {
    try {
      Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
      expressionAttributeValues.put(
          ":emailVal", AttributeValue.builder().s(user.getEmail()).build());

      Expression filterExpression =
          Expression.builder()
              .expression("email = :emailVal")
              .expressionValues(expressionAttributeValues)
              .build();

      ScanEnhancedRequest scanRequest =
          ScanEnhancedRequest.builder().filterExpression(filterExpression).build();

      User actual = utility.get(scanRequest);

    } catch (DynamoDbException e) {
      e.printStackTrace();
      fail("fail");
    }
  }

  @DisplayName("Can delete an item from DynamoDB \uD83E\uDD8D")
  @Test
  public void deleteItem() {

    try {
      final String localId = "willbedeleted";

      User newUser = new User(localId, "email@gmail.com", "aa", "foo");
      utility.post(newUser);
      utility.delete(localId);

      User actual = utility.get(localId);
      assertNull(actual);
    } catch (DynamoDbException e) {
      e.printStackTrace();
      fail("fail");
    }
  }

  @DisplayName("Can update an item from DynamoDB \uD83E\uDD8D")
  @Test
  public void patchItem() {
    User updateValues = new User(userId, null, "newfakePassword", null);

    try {
      utility.patch(updateValues);

      User actual = utility.get(userId);
      assertNotNull(actual);

      assertEquals(actual.getPassword(), "newfakePassword");
    } catch (DynamoDbException e) {
      e.printStackTrace();
      fail("fail");
    }
  }

  @DisplayName("Can list items from DynamoDB \uD83E\uDD8D")
  @Test
  public void listItems() {
    HashMap<String, AttributeValue> expressionAttributeValues = new HashMap<>();
    expressionAttributeValues.put(":passwordVal", AttributeValue.builder().s("matching").build());

    Expression filterExpression =
        Expression.builder()
            .expression("password = :passwordVal")
            .expressionValues(expressionAttributeValues)
            .build();

    ScanEnhancedRequest scanRequest =
        ScanEnhancedRequest.builder().filterExpression(filterExpression).build();

    try {
      List<User> actualItems = utility.list(scanRequest);

      assertNotNull(actualItems);
      assertTrue(actualItems.size() >= 2);
    } catch (DynamoDbException e) {
      e.printStackTrace();
      fail("fail");
    }
  }
}
