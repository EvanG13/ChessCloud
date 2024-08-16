package org.example.entities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;
import org.bson.Document;

public class Connection extends DataTransferObject {
  @Expose private String username;
  @Expose private String connectionId;

  public Connection(String username, String connectionId) {
    super(connectionId);
    this.username = username;
    this.connectionId = connectionId;
  }

  public static Connection fromDocument(Document connectionDocument) {
    return new Connection(
        connectionDocument.getString("connectionId"), connectionDocument.getString("username"));
  }

  @Override
  public Document toDocument() {
    return new Document()
        .append("connectionId", this.connectionId)
        .append("username", this.username);
  }

  @Override
  public String toResponseJson() {
    Gson gsonBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    return gsonBuilder.toJson(this, Connection.class);
  }

  @Override
  public String toString() {
    return username + " " + " " + connectionId;
  }
}
