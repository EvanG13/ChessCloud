package org.example.services;

import static com.mongodb.client.model.Filters.*;

import com.mongodb.client.model.Filters;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.bson.conversions.Bson;
import org.example.constants.ChessConstants;
import org.example.entities.Game;
import org.example.entities.Stats;
import org.example.entities.User;
import org.example.enums.GameStatus;
import org.example.enums.TimeControl;
import org.example.utils.MongoDBUtility;

@AllArgsConstructor
public class JoinGameService {
  private final MongoDBUtility<Game> gameDBUtility;
  private final MongoDBUtility<User> userDBUtility;
  private final MongoDBUtility<Stats> statsDBUtility;

  public JoinGameService() {
    this.gameDBUtility = new MongoDBUtility<>("games", Game.class);
    this.userDBUtility = new MongoDBUtility<>("users", User.class);
    this.statsDBUtility = new MongoDBUtility<>("stats", Stats.class);
  }

  public Optional<Game> getPendingGame(TimeControl timeControl, int rating) {
    Optional<Game> optionalGame =
        gameDBUtility.get(
            Filters.and(
                eq("timeControl", timeControl),
                eq("gameStatus", GameStatus.PENDING),
                gte("rating", rating - ChessConstants.RATING_MARGIN),
                lte("rating", rating + ChessConstants.RATING_MARGIN)));

    if (optionalGame.isEmpty()) {
      return Optional.empty();
    }

    return optionalGame;
  }

  public boolean isInGame(String userId) {
    Bson filter = Filters.elemMatch("players", Filters.eq("playerId", userId));
    Optional<Game> optionalGame = gameDBUtility.get(filter);
    return optionalGame.isPresent();
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
