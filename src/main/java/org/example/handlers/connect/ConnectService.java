package org.example.handlers.connect;

import com.mongodb.client.model.Filters;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.example.databases.MongoDBUtility;
import org.example.entities.Connection;
import org.example.requestRecords.ConnectionRequest;

@AllArgsConstructor
public class ConnectService {
  private final MongoDBUtility<Connection> utility;

  public ConnectService() {
    this.utility = new MongoDBUtility<>("connections", Connection.class);
  }

  public boolean doesConnectionExistByUsername(String username) {
    Optional<Connection> conn = utility.get(Filters.eq("username", username));

    return conn.isPresent();
  }

  public boolean doesConnectionExistById(String connectionId) {
    Optional<Connection> conn = utility.get(connectionId);

    return conn.isPresent();
  }

  public void createConnection(ConnectionRequest data) {

    Connection newConnection =
        Connection.builder().id(data.connectionId()).username(data.username()).build();

    utility.post(newConnection);
  }
}
