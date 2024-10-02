package org.example.entities.stats;

import java.util.Optional;
import org.example.entities.user.User;
import org.example.exceptions.InternalServerError;
import org.example.utils.MongoDBUtility;

public class StatsService {
  private final MongoDBUtility<User> userDBUtility;
  private final MongoDBUtility<Stats> statsDBUtility;

  public StatsService() {
    this.userDBUtility = new MongoDBUtility<>("users", User.class);
    this.statsDBUtility = new MongoDBUtility<>("stats", Stats.class);
  }

  public boolean doesUserExist(String userId) {
    Optional<User> optionalUser = userDBUtility.get(userId);
    return optionalUser.isPresent();
  }

  public Stats getStatsByUserID(String userId) throws InternalServerError {
    return statsDBUtility
        .get(userId)
        .orElseThrow(() -> new InternalServerError("Missing User's Stats"));
  }

  public void deleteStats(String id) {
    statsDBUtility.delete(id);
  }

  public void post(Stats stats) {
    statsDBUtility.post(stats);
  }
}
