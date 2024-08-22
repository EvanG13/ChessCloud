package org.example.handlers.connect;

import java.util.Optional;
import lombok.AllArgsConstructor;
import org.example.databases.ConnectionMongoDBUtility;
import org.example.databases.MongoDBUtility;
import org.example.entities.Connection;
import org.example.requestRecords.ConnectionRequest;

@AllArgsConstructor
public class ConnectService {
  private final ConnectionMongoDBUtility utility;

  public ConnectService() {
    this.utility = new ConnectionMongoDBUtility(MongoDBUtility.getInstance("connections"));
  }

  public boolean doesConnectionExistByUsername(String username) {
    Optional<Connection> conn = utility.getByUsername(username);

    return conn.isPresent();
  }

  public boolean doesConnectionExistById(String connectionId) {
    Optional<Connection> conn = utility.get(connectionId);

    return conn.isPresent();
  }

  public void createConnection(String username, String connectionId) {
    ConnectionRequest newConnection = new ConnectionRequest(username, connectionId);
    utility.post(newConnection);
  }
}
