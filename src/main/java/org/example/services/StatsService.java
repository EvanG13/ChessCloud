package org.example.services;

import java.util.Optional;
import org.example.entities.Stats;
import org.example.entities.User;
import org.example.exceptions.InternalServerError;
import org.example.utils.MongoDBUtility;

public class StatsService {
  private final MongoDBUtility<User> userDBUtility;
  private final MongoDBUtility<Stats> statsDBUtility;

  public StatsService() {
    this.userDBUtility = new MongoDBUtility<>("users", User.class);
    this.statsDBUtility = new MongoDBUtility<>("stats", Stats.class);
  }

  public boolean doesUserExist(String id) {
    Optional<User> optionalUser = userDBUtility.get(id);
    return optionalUser.isPresent();
  }

  public Stats getStatsByUserID(String id) throws InternalServerError {
    return statsDBUtility
        .get(id)
        .orElseThrow(() -> new InternalServerError("Missing User's Stats"));
  }
}
