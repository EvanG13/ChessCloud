package org.example.databases;

import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.ObjectId;
import org.example.entities.Connection;
import org.example.requestRecords.ConnectionRequest;

public class ConnectionMongoDBUtility {
  private final MongoDBUtility utility;

  public ConnectionMongoDBUtility() {
    utility = MongoDBUtility.getInstance("connections");
  }

  public ConnectionMongoDBUtility(MongoDBUtility utility) {
    this.utility = utility;
  }

  /**
   * Get a Connection by their connectionId
   *
   * @param id id
   * @return Connection
   */
  public Connection get(String id) {
    Document doc = utility.get(id);
    if (doc == null) {
      return null;
    }

    return Connection.fromDocument(doc);
  }

  public Connection getByUsername(String username) {
    Document conn = utility.get(Filters.eq("username", username));

    if (conn == null) {
      return null;
    }

    return Connection.fromDocument(conn);
  }

  public Connection getByConnectionId(String connectionId) {
    Document conn = utility.get(Filters.eq("connectionId", connectionId));

    if (conn == null) {
      return null;
    }

    return Connection.fromDocument(conn);
  }

  public void deleteAllDocuments() {
    utility.delete();
  }

  /**
   * Create a new Connection
   *
   * @param connectionData user request data object
   */
  public void post(ConnectionRequest connectionData) {
    utility.post(
        new Document("_id", new ObjectId())
            .append("connectionId", connectionData.connectionId())
            .append("username", connectionData.username()));
  }

  /**
   * Deletes a Connection by their id
   *
   * @param id id
   */
  public void deleteByConnectionId(String id) {
    utility.deleteByIndex("connectionId", id);
  }

  /**
   * Deletes a Connection by their id
   *
   * @param id id
   */
  public void delete(String id) {
    utility.delete(id);
  }

  /**
   * Deletes a Connection by their username
   *
   * @param username username
   */
  public void deleteByUsername(String username) {
    utility.deleteByIndex("username", username);
  }

  /**
   * Update a connection with the given id
   *
   * @param id object id
   * @param filter filter
   */
  public void patch(String id, Bson filter) {
    utility.patch(id, filter);
  }
}
