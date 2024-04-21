package org.example.entities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import org.bson.Document;
import org.bson.types.ObjectId;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSecondaryPartitionKey;

@DynamoDbBean
public class User extends DataTransferObject {
  @Expose private String email;
  private String password;
  @Expose private String username;

  /**
   * !Please do NOT remove this. The DynamoDB enhanced client requires having a default constructor
   */
  public User() {}

  public User(String id, String email, String password, String username) {
    super(id);
    this.email = email;
    this.password = password;
    this.username = username;
  }

  public static User fromDocument(Document userDocument) {
    return new User(
        String.valueOf(userDocument.getObjectId("_id")),
        userDocument.getString("email"),
        userDocument.getString("password"),
        userDocument.getString("username"));
  }

  @DynamoDbAttribute(value = "email")
  @DynamoDbSecondaryPartitionKey(indexNames = "emailPasswordIndex")
  public String getEmail() {
    return this.email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @DynamoDbAttribute(value = "password")
  public String getPassword() {
    return this.password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  @DynamoDbAttribute(value = "username")
  public String getUsername() {
    return this.username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @Override
  public Document toDocument() {
    return new Document("_id", new ObjectId(id))
        .append("email", email)
        .append("password", password)
        .append("username", username);
  }

  @Override
  public String toResponseJson() {
    Gson gsonBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    return gsonBuilder.toJson(this, User.class);
  }
}
