package org.example.databases;

import java.util.*;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

public class DynamoDBUtility {
  private final String PRIMARY_KEY_ATTRIBUTE_NAME = "id";

  private final DynamoDbClient client;

  private final String tableName;

  public DynamoDBUtility(String tableName) {
    this.tableName = tableName;

    this.client =
        DynamoDbClient.builder()
            .region(Region.US_EAST_1)
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();
  }

  /**
   * Get a DynamoDB item by its ID
   *
   * @param id id
   * @return DynamoDB item
   */
  public Map<String, AttributeValue> get(String id) {

    HashMap<String, AttributeValue> keyToGet = new HashMap<>();
    keyToGet.put(PRIMARY_KEY_ATTRIBUTE_NAME, AttributeValue.builder().s(id).build());

    GetItemRequest request =
        GetItemRequest.builder().key(keyToGet).tableName(this.tableName).build();

    try {
      return this.client.getItem(request).item();
    } catch (DynamoDbException e) {
      e.printStackTrace();
      throw e;
    }
  }

  public Map<String, AttributeValue> get(Map<String, AttributeValue> map) {
    GetItemRequest request = GetItemRequest.builder().key(map).tableName(this.tableName).build();

    try {
      return this.client.getItem(request).item();
    } catch (DynamoDbException e) {
      e.printStackTrace();
      throw e;
    }
  }

  /**
   * Create new DynamoDB item
   *
   * @param newItem item request data
   */
  public void post(Map<String, AttributeValue> newItem) {
    PutItemRequest request =
        PutItemRequest.builder().tableName(this.tableName).item(newItem).build();

    try {
      PutItemResponse response = this.client.putItem(request);
      System.out.println(
          this.tableName
              + " was successfully updated. The request id is "
              + response.responseMetadata().requestId());

    } catch (ResourceNotFoundException e) {
      System.err.format("Error: The Amazon DynamoDB table \"%s\" can't be found.\n", tableName);
      System.err.println("Be sure that it exists and that you've typed its name correctly!");
      System.exit(1);
    } catch (DynamoDbException e) {
      System.err.println(e.getMessage());
      throw e;
    }
  }

  public void patch(String id, Map<String, AttributeValueUpdate> updatedValues) {

    HashMap<String, AttributeValue> keyToGet = new HashMap<>();
    keyToGet.put(PRIMARY_KEY_ATTRIBUTE_NAME, AttributeValue.builder().s(id).build());

    UpdateItemRequest request =
        UpdateItemRequest.builder()
            .tableName(this.tableName)
            .key(keyToGet)
            .attributeUpdates(updatedValues)
            .build();

    try {
      client.updateItem(request);
    } catch (DynamoDbException e) {
      System.err.println(e.getMessage());
      throw e;
    }
  }

  public List<Map<String, AttributeValue>> list(ScanRequest req) {
    try {
      ScanResponse scanResponse = client.scan(req);

      return scanResponse.items();
    } catch (DynamoDbException e) {
      System.err.println(e.getMessage());
      throw e;
    }
  }

  public void delete(String id) {
    HashMap<String, AttributeValue> keyToGet = new HashMap<>();
    keyToGet.put(PRIMARY_KEY_ATTRIBUTE_NAME, AttributeValue.builder().s(id).build());

    DeleteItemRequest deleteReq =
        DeleteItemRequest.builder().tableName(this.tableName).key(keyToGet).build();

    try {
      client.deleteItem(deleteReq);
    } catch (DynamoDbException e) {
      System.err.println(e.getMessage());
      throw e;
    }
  }

  public DynamoDbClient getClient() {
    return client;
  }
}
