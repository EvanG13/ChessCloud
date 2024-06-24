package org.example.entities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;
import org.bson.types.ObjectId;

@Setter
@Getter
public class User extends DataTransferObject {
  @Expose private String email;
  private String password;
  @Expose private String username;

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

  @Override
  public Document toDocument() {
    return new Document("_id", new ObjectId(id))
        .append("email", email)
        .append("password", password)
        .append("username", username);
  }

  @Override
  public String toString() {
    return email + " " + username + " " + id;
  }

  @Override
  public String toResponseJson() {
    Gson gsonBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    return gsonBuilder.toJson(this, User.class);
  }
}
