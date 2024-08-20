package org.example.handlers.connect;

import org.example.databases.ConnectionMongoDBUtility;
import org.example.databases.MongoDBUtility;
import org.example.entities.Connection;
import org.example.requestRecords.ConnectionRequest;

public class ConnectService {
  private final ConnectionMongoDBUtility utility;

  public ConnectService(ConnectionMongoDBUtility dbUtility) {
    this.utility = dbUtility;
  }

  public ConnectService() {
    this.utility = new ConnectionMongoDBUtility(MongoDBUtility.getInstance("connections"));
  }

  public boolean doesConnectionExist(String username) {
    Connection conn = utility.getByUsername(username);

    return conn != null;
  }

  public boolean doesConnectionIdExist(String connectionId) {
    Connection conn = utility.getByConnectionId(connectionId);

    return conn != null;
  }

  public void createConnection(String username, String connectionId) {
    ConnectionRequest newConnection = new ConnectionRequest(username, connectionId);
    utility.post(newConnection);
  }
}
