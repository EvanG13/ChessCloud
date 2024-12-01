package org.example.entities.stats;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.*;

import org.bson.conversions.Bson;
import org.example.entities.user.User;
import org.example.entities.user.UserDbService;
import org.example.exceptions.InternalServerError;
import org.example.exceptions.NotFound;
import org.example.utils.MongoDBUtility;

public class StatsDbService {
  private final UserDbService userDbService;
  private final MongoDBUtility<Stats> statsDBUtility;

  public StatsDbService() {
    this.userDbService = new UserDbService();
    this.statsDBUtility = new MongoDBUtility<>("stats", Stats.class);
  }

  public Stats getStatsByUsername(String username) throws NotFound {

    User user = userDbService.getByUsername(username);

    return statsDBUtility
        .get(user.getId())
        .orElseThrow(() -> new NotFound("No stats found for user " + username));
  }

  public boolean doesCategoryExist(String gameCategory) {
    return "blitz".equals(gameCategory)
        || "bullet".equals(gameCategory)
        || "rapid".equals(gameCategory);
  }

  public Stats getStatsByUsernameAndCategory(String username, String gameCategory) throws NotFound {

    User user = userDbService.getByUsername(username);

    Bson projection = fields(include("gameModeStats." + gameCategory), excludeId());

    return statsDBUtility
        .get(eq("_id", user.getId()), projection)
        .orElseThrow(() -> new NotFound("No stats found for user " + username));
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
