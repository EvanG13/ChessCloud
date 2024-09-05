package org.example.handlers.stats;

import java.util.Optional;
import org.example.databases.MongoDBUtility;
import org.example.entities.User;

public class StatsService {
  private final MongoDBUtility<User> dbUtility;

  public StatsService() {
    this.dbUtility = new MongoDBUtility<>("users", User.class);
  }

  public Optional<User> getByID(String id) {
    return dbUtility.get(id);
  }
}
