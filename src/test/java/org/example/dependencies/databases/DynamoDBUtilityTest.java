package org.example.dependencies.databases;

import org.example.databases.DynamoDBUtility;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import software.amazon.awssdk.core.waiters.WaiterResponse;
import software.amazon.awssdk.services.dynamodb.model.*;
import software.amazon.awssdk.services.dynamodb.waiters.DynamoDbWaiter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class DynamoDBUtilityTest {
    private static final String COLLECTION_NAME = "testCollection";

   static DynamoDBUtility utility = new DynamoDBUtility("user");
   static HashMap<String, AttributeValue> newUser = new HashMap<>();
    static String userId = "194e4010-d49b-496e-bed8-b96c713e2110";
    @BeforeAll
    public static void setUp() {

        //create the user table
        DynamoDbWaiter dbWaiter = utility.getDdb().waiter();
        CreateTableRequest request = CreateTableRequest.builder()
                .attributeDefinitions(AttributeDefinition.builder()
                        .attributeName("id")
                        .attributeType(ScalarAttributeType.S)
                        .attributeName("email")
                        .attributeType(ScalarAttributeType.S)
                        .attributeName("password")
                        .attributeType(ScalarAttributeType.S)
                        .build())
                .keySchema(KeySchemaElement.builder()
                        .attributeName("id")
                        .keyType(KeyType.HASH)
                        .build())
                .provisionedThroughput(ProvisionedThroughput.builder()
                        .readCapacityUnits(10L)
                        .writeCapacityUnits(10L)
                        .build())
                .tableName("user")
                .build();

        String newTable;
        try {
            CreateTableResponse response = utility.getDdb().createTable(request);
            DescribeTableRequest tableRequest = DescribeTableRequest.builder()
                    .tableName("user")
                    .build();

            // Wait until the Amazon DynamoDB table is created.
            WaiterResponse<DescribeTableResponse> waiterResponse = dbWaiter.waitUntilTableExists(tableRequest);
            waiterResponse.matched().response().ifPresent(System.out::println);
            newTable = response.tableDescription().tableName();

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());

        }
        newUser.put("id", AttributeValue.builder()
                .s(userId)
                .build());
       newUser.put("email", AttributeValue.builder()
               .s("cleve@gmail.com")
               .build());
       newUser.put("password", AttributeValue.builder()
               .s("1234")
               .build());

        utility.post(newUser);

        newUser.put("id", AttributeValue.builder()
                .s("foo")
                .build());
        newUser.put("email", AttributeValue.builder()
                .s("cleve@gmail.com")
                .build());
        newUser.put("password", AttributeValue.builder()
                .s("1234")
                .build());

        utility.post(newUser);
    }

    @DisplayName("Can get an item from dynamoDB \uD83E\uDD8D")
    @Test
    public void getItem() {
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

    @DisplayName("Can delete a document from DynamoDB \uD83E\uDD8D")
    @Test
    public void deleteDocument() {

        try {
            utility.delete(userId);

            Map<String, AttributeValue> actual = utility.get(userId);
            assertEquals(0, actual.size());
        } catch (DynamoDbException e) {
            e.printStackTrace();
            fail("fail");
        }
    }

    @DisplayName("Can update a document from MongoDB \uD83E\uDD8D")
    @Test
    public void patchDocument() {
        Map<String, AttributeValueUpdate> newEmail = new HashMap<>();
        newEmail.put("email", AttributeValueUpdate.builder()
                .value(AttributeValue.builder().s("new-fake-email@gmail.com").build())
                .action(AttributeAction.PUT)
                .build());

        try {
            utility.patch(newEmail, userId);

            Map<String, AttributeValue> actual = utility.get(userId);
            assertNotNull(actual);

            assertEquals(actual.get("email").s(), "new-fake-email@gmail.com");
        } catch (DynamoDbException e) {
            e.printStackTrace();
            fail("fail");
        }
    }

    @DisplayName("Can list documents from DynamoDB \uD83E\uDD8D")
    @Test
    public void listDocuments() {
        HashMap<String, AttributeValue> user2 = new HashMap<>();
        HashMap<String, AttributeValue> user3 = new HashMap<>();
        String user2Id = "e3c9ef65-a29c-4366-b54d-2c89d9e4ffdf";
        String user3Id = "ff3c1a53-fade-4328-82ad-66ec7f89fde8";
        user2.put("id", AttributeValue.builder()
                .s(user2Id)
                .build());
        user2.put("email", AttributeValue.builder()
                .s("cleve2@gmail.com")
                .build());
        user2.put("password", AttributeValue.builder()
                .s("123")
                .build());

        user3.put("id", AttributeValue.builder()
                .s(user3Id)
                .build());
        user3.put("email", AttributeValue.builder()
                .s("cleve3@gmail.com")
                .build());
        user3.put("password", AttributeValue.builder()
                .s("123")
                .build());
        HashMap<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":passwordVal", AttributeValue.builder().s("123").build());
        ScanRequest scanRequest = ScanRequest.builder()
            .tableName("user")
            .filterExpression("password = :passwordVal")
            .expressionAttributeValues(expressionAttributeValues)
            .projectionExpression("email, password, id") // Specify the attributes you want to retrieve
            .build();

        try {
            utility.post(user2);
            utility.post(user3);

            List<Map<String, AttributeValue>> actualItems = utility.list(scanRequest);

            assertNotNull(actualItems);
            assertTrue( actualItems.size() >= 2);
        } catch (DynamoDbException e) {
            e.printStackTrace();
            fail("fail");
        }
    }
}
