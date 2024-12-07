package org.example.entities.stats;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.*;

import org.bson.conversions.Bson;
import org.example.entities.user.User;
import org.example.entities.user.UserUtility;
import org.example.enums.GameMode;
import org.example.exceptions.InternalServerError;
import org.example.exceptions.NotFound;
import org.example.utils.MongoDBUtility;

public class StatsUtility extends MongoDBUtility<Stats> {
  private final UserUtility userUtility;

  public StatsUtility() {
    super("stats", Stats.class);
    userUtility = new UserUtility();
  }

  public StatsUtility(String collection, UserUtility userUtility) {
    super(collection, Stats.class);
    this.userUtility = userUtility;
  }

  public Stats getStatsByUsername(String username) throws NotFound {
    User user = userUtility.getByUsername(username);
    return get(user.getId()).orElseThrow(() -> new NotFound("No stats found for user " + username));
  }

  public boolean doesGameModeExist(String gameMode) {
    return GameMode.fromKey(gameMode) != null;
  }

  public Stats getStatsByUserID(String userId) throws InternalServerError {
    return get(userId)
        .orElseThrow(() -> new InternalServerError("Missing User's Stats for userId: " + userId));
  }

  public Stats getStatsByCategory(String username, String category) throws NotFound {
    User user = userUtility.getByUsername(username);

    return get(user.getId()).orElseThrow(() -> new NotFound("No stats found for user " + username));
  }

  public Stats getStatsByUsernameAndCategory(String username, String gameCategory) throws NotFound {
    User user = userUtility.getByUsername(username);

    Bson projection = fields(include("gameModeStats." + gameCategory), excludeId());

    return get(eq("_id", user.getId()), projection)
        .orElseThrow(() -> new NotFound("No stats found for user " + username));
  }
}
