package org.example.services;

import static com.mongodb.client.model.Filters.*;

import com.mongodb.client.model.Filters;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.bson.conversions.Bson;
import org.example.constants.ChessConstants;
import org.example.entities.game.Game;
import org.example.entities.game.GameService;
import org.example.entities.stats.Stats;
import org.example.entities.user.User;
import org.example.enums.GameStatus;
import org.example.enums.TimeControl;
import org.example.exceptions.NotFound;
import org.example.utils.MongoDBUtility;

@AllArgsConstructor
public class JoinGameService {
  private final GameService gameDBUtility;
  private final MongoDBUtility<User> userDBUtility;
  private final MongoDBUtility<Stats> statsDBUtility;

  public JoinGameService() {
    this.gameDBUtility = new GameService();
    this.userDBUtility = new MongoDBUtility<>("users", User.class);
    this.statsDBUtility = new MongoDBUtility<>("stats", Stats.class);
  }

  public Game getPendingGame(TimeControl timeControl, int rating) throws NotFound {
    return gameDBUtility.get(
        Filters.and(
            eq("timeControl", timeControl),
            eq("gameStatus", GameStatus.PENDING),
            gte("rating", rating - ChessConstants.RATING_MARGIN),
            lte("rating", rating + ChessConstants.RATING_MARGIN)));
  }

  public boolean isInGame(String userId) {
    Bson filter = Filters.elemMatch("players", Filters.eq("playerId", userId));
    try {
      gameDBUtility.get(filter);
    } catch (NotFound e) {
      return false;
    }

    return true;
  }

  public void createGame(Game game) {
    gameDBUtility.post(game);
  }

  public void updateGame(Game game) {
    gameDBUtility.put(game.getId(), game);
  }

  public Optional<User> getUser(String userId) {
    Optional<User> user = userDBUtility.get(userId);
    if (user.isEmpty()) {
      return Optional.empty();
    }
    return user;
  }

  public Optional<Stats> getUserStats(String userId) {
    Optional<Stats> stats = statsDBUtility.get(userId);
    if (stats.isEmpty()) {
      return Optional.empty();
    }
    return stats;
  }
}
