package org.example.entities.connection;

import org.example.utils.MongoDBUtility;

public class ConnectionUtility extends MongoDBUtility<Connection> {
  public ConnectionUtility() {
    super("connections", Connection.class);
  }

  public ConnectionUtility(String collection) {
    super(collection, Connection.class);
  }
}
