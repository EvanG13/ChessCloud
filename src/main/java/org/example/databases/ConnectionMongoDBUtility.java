package org.example.databases;

import com.mongodb.client.model.Filters;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.example.entities.Connection;
import org.example.requestRecords.ConnectionRequest;

@AllArgsConstructor
public class ConnectionMongoDBUtility {
  private final MongoDBUtility utility;

  public ConnectionMongoDBUtility() {
    utility = MongoDBUtility.getInstance("connections");
  }

  public Optional<Connection> get(String id) {
    Document conn = utility.get(Filters.eq("_id", id));
    if (conn == null) {
      return Optional.empty();
    }

    return Optional.of(Connection.fromDocument(conn));
  }

  public Optional<Connection> getByUsername(String username) {
    Document conn = utility.get(Filters.eq("username", username));
    if (conn == null) {
      return Optional.empty();
    }

    return Optional.of(Connection.fromDocument(conn));
  }

  public void post(ConnectionRequest connectionData) {
    utility.post(
        new Document("_id", connectionData.connectionId())
            .append("username", connectionData.username()));
  }

  public void delete(String id) {
    utility.deleteByIndex("_id", id);
  }

  public void deleteByUsername(String username) {
    utility.deleteByIndex("username", username);
  }

  public void patch(String id, Bson filter) {
    utility.patch(id, filter);
  }
}
