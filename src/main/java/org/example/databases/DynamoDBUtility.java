package org.example.databases;

import java.util.*;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.pagination.sync.SdkIterable;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.*;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

public class DynamoDBUtility<T> {

  private final DynamoDbTable<T> table;

  private final Class<T> type;

  public DynamoDBUtility(DynamoDbTable<T> table, Class<T> type) {

    this.type = type;

    this.table = table;
  }

  public static <T> DynamoDBUtility<T> create(String tableName, Class<T> type) {
    DynamoDbClient client =
        DynamoDbClient.builder()
            .region(Region.US_EAST_1)
            .httpClient(ApacheHttpClient.builder().build())
            .credentialsProvider(DefaultCredentialsProvider.create())
            .build();

    DynamoDbEnhancedClient enhancedClient =
        DynamoDbEnhancedClient.builder().dynamoDbClient(client).build();

    DynamoDbTable<T> table = enhancedClient.table(tableName, TableSchema.fromBean(type));

    return new DynamoDBUtility<>(table, type);
  }

  /**
   * Get a DynamoDB item by its ID
   *
   * @param id id
   * @return DynamoDB item
   */
  public T get(String id) {
    try {
      return this.table.getItem(Key.builder().partitionValue(id).build());
    } catch (DynamoDbException e) {
      System.out.println(e.getMessage());
      if (e.statusCode() == 403) {
        System.out.println("unauthorized");
      }
      throw e;
    }
  }

  public T get(ScanEnhancedRequest scanRequest) {
    try {
      PageIterable<T> items = table.scan(scanRequest);
      Iterator<T> iterator = items.items().iterator();

      if (iterator.hasNext()) {
        return iterator.next();
      }

      return null;
    } catch (DynamoDbException e) {
      e.printStackTrace();
      throw e;
    }
  }

  public T get(QueryConditional queryConditional, String indexName) {
    try {

      DynamoDbIndex<T> index = table.index(indexName);

      SdkIterable<Page<T>> pagedResult = index.query(q -> q.queryConditional(queryConditional));

      List<T> queriedItems = new ArrayList<>();

      pagedResult.stream().forEach(page -> queriedItems.addAll(page.items()));

      return queriedItems.isEmpty() ? null : queriedItems.get(0);
    } catch (DynamoDbException e) {
      e.printStackTrace();
      throw e;
    }
  }

  /**
   * Create new DynamoDB item
   *
   * @param item item request data
   */
  public void post(T item) {
    try {
      PutItemEnhancedRequest<T> request = PutItemEnhancedRequest.builder(type).item(item).build();

      table.putItem(request);
    } catch (DynamoDbException e) {
      System.out.println(e.getMessage());
      throw e;
    }
  }

  public void patch(T updateItem) {
    try {
      table.updateItem(a -> a.item(updateItem).ignoreNulls(Boolean.TRUE));
    } catch (DynamoDbException e) {
      System.err.println(e.getMessage());
      throw e;
    }
  }

  public List<T> list(ScanEnhancedRequest scanRequest) {
    try {
      PageIterable<T> items = table.scan(scanRequest);
      return items.items().stream().toList();
    } catch (DynamoDbException e) {
      System.err.println(e.getMessage());
      throw e;
    }
  }

  public void delete(String id) {
    try {
      table.deleteItem(Key.builder().partitionValue(id).build());
    } catch (DynamoDbException e) {
      System.err.println(e.getMessage());
      throw e;
    }
  }
}
