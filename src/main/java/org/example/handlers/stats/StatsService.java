package org.example.handlers.stats;

import java.util.Optional;
import org.example.databases.MongoDBUtility;
import org.example.entities.Stats;
import org.example.entities.User;

public class StatsService {
  private final MongoDBUtility<User> userDBUtility;
  private final MongoDBUtility<Stats> statsDBUtility;

  public StatsService() {
    this.userDBUtility = new MongoDBUtility<>("users", User.class);
    this.statsDBUtility = new MongoDBUtility<>("stats", Stats.class);
  }

  public Optional<User> getUserByID(String id) {
    return userDBUtility.get(id);
  }

  public Optional<Stats> getStatsByUserID(String id) {
    return statsDBUtility.get(id);
  }
}
