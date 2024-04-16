package org.example.entities;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.GetItemRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

public class DynamoUser {
    private String id;
    private String username;
    private String email;
    private String password;

    // Constructor
    public DynamoUser(String id, String email, String username, String password) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.password = password;
    }

    // Getters and setters omitted for brevity

    // Method to convert a DynamoDB item to a User object
    public static DynamoUser fromMap(Map<String, AttributeValue> item) {
        String id = item.get("id").s();
        String email = item.get("email").s();
        String username = item.get("username").s();
        String password = item.get("password").s();
        return new DynamoUser(id, email, username, password);
    }

    public Map<String, AttributeValue> toMap() {
        Map<String, AttributeValue> itemMap = new HashMap<>();
        itemMap.put("id", AttributeValue.builder().s(this.id).build());
        itemMap.put("email", AttributeValue.builder().s(this.email).build());
        itemMap.put("username", AttributeValue.builder().s(this.username).build());
        itemMap.put("password", AttributeValue.builder().s(this.password).build());

        return itemMap;
    }
}
