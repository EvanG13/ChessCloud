package org.example.entities.stats;

import java.util.Optional;

import org.bson.conversions.Bson;
import org.example.entities.user.User;
import org.example.exceptions.InternalServerError;
import org.example.utils.MongoDBUtility;

public class StatsDbService {
  private final MongoDBUtility<User> userDBUtility;
  private final MongoDBUtility<Stats> statsDBUtility;

  public StatsDbService() {
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
        .orElseThrow(() -> new InternalServerError("Missing User's Stats for userId: " + userId));
  }

  public void deleteStats(String id) {
    statsDBUtility.delete(id);
  }

  public void post(Stats stats) {
    statsDBUtility.post(stats);
  }

  public void put(String id, Stats stats) {
    statsDBUtility.put(id, stats);
  }

  public void patch(String id, Bson filter) {
    statsDBUtility.patch(id, filter);
  }
}
