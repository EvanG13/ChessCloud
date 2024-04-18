package org.example.entities;

import java.util.HashMap;
import java.util.Map;
import org.bson.Document;
import org.bson.types.ObjectId;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class User extends DataTransferObject {
  private final String email;
  private final String password;
  private final String username;

  public User(String id, String email, String password, String username) {
    super(id);
    this.email = email;
    this.password = password;
    this.username = username;
  }

  public String getEmail() {
    return this.email;
  }

  public String getPassword() {
    return this.password;
  }

  public Document toDocument() {
    return new Document("_id", new ObjectId(id))
        .append("email", email)
        .append("password", password)
        .append("username", username);
  }

  public static User fromDocument(Document userDocument) {
    return new User(
        String.valueOf(userDocument.getObjectId("_id")),
        userDocument.getString("email"),
        userDocument.getString("password"),
        userDocument.getString("username"));
  }

  public static User fromMap(Map<String, AttributeValue> item) {
    String id = item.get("id").s();
    String email = item.get("email").s();
    String username = item.get("username").s();
    String password = item.get("password").s();
    return new User(id, email, username, password);
  }

  public Map<String, AttributeValue> toMap() {
    Map<String, AttributeValue> itemMap = new HashMap<>();
    itemMap.put("id", AttributeValue.builder().s(this.id).build());
    itemMap.put("email", AttributeValue.builder().s(this.email).build());
    itemMap.put("username", AttributeValue.builder().s(this.username).build());
    itemMap.put("password", AttributeValue.builder().s(this.password).build());

    return itemMap;
  }

  @Override
  public String toString() {
    return "email " + email + " - " + "Password " + password + "\n";
  }
}
