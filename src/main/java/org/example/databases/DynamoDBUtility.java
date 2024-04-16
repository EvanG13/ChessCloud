package org.example.databases;


import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.EnvironmentVariableCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import java.util.*;


public class DynamoDBUtility {
    private final String PRIMARY_KEY_ATTRIBUTE_NAME = "id";

    private Region region;
    private String tableName;
    private DynamoDbClient ddb;
    public DynamoDBUtility(String tableName) {
        this.tableName = tableName;
        this.region = Region.US_EAST_1;
        this.ddb = DynamoDbClient.builder().region(this.region)
          .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

    }

    public DynamoDbClient getDdb() {
        return ddb;
    }

    public Map<String, AttributeValue> get(String id) {

        HashMap<String, AttributeValue> keyToGet = new HashMap<>();
        keyToGet.put(PRIMARY_KEY_ATTRIBUTE_NAME, AttributeValue.builder()
                .s(id)
                .build());

        GetItemRequest request = GetItemRequest.builder()
                .key(keyToGet)
                .tableName(this.tableName)
                .build();

        try {
            Map<String, AttributeValue> returnedItem = this.ddb.getItem(request).item();

            if (returnedItem != null) {
                Set<String> keys = returnedItem.keySet();
                System.out.println("Amazon DynamoDB table attributes: \n");

                for (String key1 : keys) {
                    System.out.format("%s: %s\n", key1, returnedItem.get(key1).toString());
                }
            } else {
                System.out.format("No item found with the key %s!\n", "year");
            }
            return returnedItem;

        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            throw e;
        }
    }



    public void delete(String id) {
        HashMap<String, AttributeValue> keyToGet = new HashMap<>();
        keyToGet.put(PRIMARY_KEY_ATTRIBUTE_NAME, AttributeValue.builder()
                .s(id)
                .build());

        DeleteItemRequest deleteReq = DeleteItemRequest.builder()
                .tableName(this.tableName)
                .key(keyToGet)
                .build();

        try {
            ddb.deleteItem(deleteReq);
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            throw e;
        }
    }


    public void post(Map<String, AttributeValue> newItem) {
        PutItemRequest request = PutItemRequest.builder()
                .tableName(this.tableName)
                .item(newItem)
                .build();

        try {
            PutItemResponse response = this.ddb.putItem(request);
            System.out.println(this.tableName + " was successfully updated. The request id is "
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

    public void patch(Map<String, AttributeValueUpdate> updatedValues, String id) {

        HashMap<String, AttributeValue> keyToGet = new HashMap<>();
        keyToGet.put(PRIMARY_KEY_ATTRIBUTE_NAME, AttributeValue.builder()
                .s(id)
                .build());

        UpdateItemRequest request = UpdateItemRequest.builder()
                .tableName(this.tableName)
                .key(keyToGet)
                .attributeUpdates(updatedValues)
                .build();

        try {
            ddb.updateItem(request);
        } catch (DynamoDbException e) {
            System.err.println(e.getMessage());
            throw e;
        }
        System.out.println("The Amazon DynamoDB table was updated!");
    }



//    ScanRequest scanRequest = ScanRequest.builder()
//            .tableName(tableName)
//            .filterExpression("password = :passwordVal")
//            .expressionAttributeValues(expressionAttributeValues)
//            .projectionExpression("email, password, id") // Specify the attributes you want to retrieve
//            .build();
    public List<Map<String, AttributeValue>> list(ScanRequest req) {
        try {
            ScanResponse scanResponse = ddb.scan(req);

            List<Map<String, AttributeValue>> items = scanResponse.items();
            return items;
        } catch (DynamoDbException e){
            System.err.println(e);
            throw e;
        }
    }

}
