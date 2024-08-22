package org.example.entities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import org.bson.Document;

public class Connection extends DataTransferObject {
  @Expose private String username;

  public Connection(String username, String id) {
    super(id);
    this.username = username;
  }

  public static Connection fromDocument(Document connectionDocument) {
    return new Connection(
        connectionDocument.getString("_id"), connectionDocument.getString("username"));
  }

  @Override
  public Document toDocument() {
    return new Document("_id", id).append("username", this.username);
  }

  @Override
  public String toResponseJson() {
    Gson gsonBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    return gsonBuilder.toJson(this, Connection.class);
  }

  @Override
  public String toString() {
    return this.username + " " + id;
  }
}
