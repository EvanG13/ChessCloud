package org.example.databases;

import static org.junit.jupiter.api.Assertions.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.*;
import software.amazon.awssdk.services.dynamodb.model.*;

public class DynamoDBUtilityTest {

  static DynamoDBUtility utility = DynamoDBUtility.create("users");
  static HashMap<String, AttributeValue> newUser = new HashMap<>();

  static String userId = "194e4010-d49b-496e-bed8-b96c713e2110";

  @BeforeAll
  public static void setUp() {
    newUser.put("id", AttributeValue.builder().s(userId).build());
    newUser.put("email", AttributeValue.builder().s("cleve@gmail.com").build());
    newUser.put("password", AttributeValue.builder().s("1234").build());
    utility.post(newUser);

    newUser.put("id", AttributeValue.builder().s("foo").build());
    newUser.put("email", AttributeValue.builder().s("cleve@gmail.com").build());
    newUser.put("password", AttributeValue.builder().s("1234").build());
    utility.post(newUser);
  }

  @DisplayName("Can get an item from dynamoDB \uD83E\uDD8D")
  @Test
  public void getItemById() {
    try {
      Map<String, AttributeValue> actual = utility.get("foo");
      assertNotNull(actual);

      assertEquals("foo", actual.get("id").s());
      assertEquals("cleve@gmail.com", actual.get("email").s());
      assertEquals("1234", actual.get("password").s());

    } catch (DynamoDbException e) {
      e.printStackTrace();
      fail("fail");
    }
  }

  @DisplayName("Can get an item from dynamoDB \uD83E\uDD8D")
  @Test
  public void getItemByMap() {
    try {

      Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
      expressionAttributeValues.put(
          ":emailVal", AttributeValue.builder().s(newUser.get("email").s()).build());
      expressionAttributeValues.put(
          ":passwordVal", AttributeValue.builder().s(newUser.get("password").s()).build());

      QueryRequest queryRequest =
          QueryRequest.builder()
              .tableName("users")
              .indexName("emailPasswordIndex")
              .keyConditionExpression("email = :emailVal AND password = :passwordVal")
              .expressionAttributeValues(expressionAttributeValues)
              .build();

      Map<String, AttributeValue> actual = utility.get(queryRequest);

      assertEquals(newUser.get("id").s(), actual.get("id").s());
      assertEquals(newUser.get("email").s(), actual.get("email").s());
      assertEquals(newUser.get("password").s(), actual.get("password").s());
    } catch (DynamoDbException e) {
      e.printStackTrace();
      fail("fail");
    }
  }

  @DisplayName("Can delete an item from DynamoDB \uD83E\uDD8D")
  @Test
  public void deleteItem() {

    try {
      utility.delete(userId);

      Map<String, AttributeValue> actual = utility.get(userId);
      assertEquals(0, actual.size());
    } catch (DynamoDbException e) {
      e.printStackTrace();
      fail("fail");
    }
  }

  @DisplayName("Can update an item from DynamoDB \uD83E\uDD8D")
  @Test
  public void patchItem() {
    Map<String, AttributeValueUpdate> newEmail = new HashMap<>();
    newEmail.put(
        "email",
        AttributeValueUpdate.builder()
            .value(AttributeValue.builder().s("new-fake-email@gmail.com").build())
            .action(AttributeAction.PUT)
            .build());

    try {
      utility.patch(userId, newEmail);

      Map<String, AttributeValue> actual = utility.get(userId);
      assertNotNull(actual);

      assertEquals(actual.get("email").s(), "new-fake-email@gmail.com");
    } catch (DynamoDbException e) {
      e.printStackTrace();
      fail("fail");
    }
  }

  @DisplayName("Can list items from DynamoDB \uD83E\uDD8D")
  @Test
  public void listItems() {
    HashMap<String, AttributeValue> user2 = new HashMap<>();
    HashMap<String, AttributeValue> user3 = new HashMap<>();

    final String user2Id = "e3c9ef65-a29c-4366-b54d-2c89d9e4ffdf";
    final String user3Id = "ff3c1a53-fade-4328-82ad-66ec7f89fde8";

    user2.put("id", AttributeValue.builder().s(user2Id).build());
    user2.put("email", AttributeValue.builder().s("cleve2@gmail.com").build());
    user2.put("password", AttributeValue.builder().s("123").build());

    user3.put("id", AttributeValue.builder().s(user3Id).build());
    user3.put("email", AttributeValue.builder().s("cleve3@gmail.com").build());
    user3.put("password", AttributeValue.builder().s("123").build());

    HashMap<String, AttributeValue> expressionAttributeValues = new HashMap<>();
    expressionAttributeValues.put(":passwordVal", AttributeValue.builder().s("123").build());

    ScanRequest scanRequest =
        ScanRequest.builder()
            .tableName("users")
            .filterExpression("password = :passwordVal")
            .expressionAttributeValues(expressionAttributeValues)
            .projectionExpression(
                "email, password, id") // Specify the attributes you want to retrieve
            .build();

    try {
      utility.post(user2);
      utility.post(user3);

      List<Map<String, AttributeValue>> actualItems = utility.list(scanRequest);

      assertNotNull(actualItems);
      assertTrue(actualItems.size() >= 2);
    } catch (DynamoDbException e) {
      e.printStackTrace();
      fail("fail");
    }
  }
}
