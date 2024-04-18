package org.example.databases;

import java.util.*;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

public class DynamoDBUtility {
  private final String PRIMARY_KEY_ATTRIBUTE_NAME = "id";

  private final DynamoDbClient client;

  private final String tableName;

  private DynamoDBUtility(String tableName, DynamoDbClient client) {
    this.tableName = tableName;
    this.client = client;
  }

  public static DynamoDBUtility create(String tableName) {
    DynamoDbClient client =
        DynamoDbClient.builder()
            .region(Region.US_EAST_1)
            .httpClient(ApacheHttpClient.builder().build())
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();

    return new DynamoDBUtility(tableName, client);
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

  public Map<String, AttributeValue> get(QueryRequest queryRequest) {
    try {
      return client.query(queryRequest).items().get(0);
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
      client.putItem(request);
    } catch (ResourceNotFoundException e) {
      throw e;
    } catch (DynamoDbException e) {
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
}
