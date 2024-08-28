package org.example.handlers.joinGame;

import static com.mongodb.client.model.Filters.*;

import com.mongodb.client.model.Filters;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.example.databases.MongoDBUtility;
import org.example.entities.Game;
import org.example.entities.User;
import org.example.utils.GameStatus;
import org.example.utils.TimeControl;

@AllArgsConstructor
public class JoinGameService {
  private final MongoDBUtility<Game> gameDBUtility;
  private final MongoDBUtility<User> userDBUtility;
  private final int RATING_MARGIN = 500;

  public JoinGameService() {
    this.gameDBUtility = new MongoDBUtility<>("games", Game.class);
    this.userDBUtility = new MongoDBUtility<>("users", User.class);
  }

  public Optional<Game> getPendingGame(TimeControl timeControl, int rating) {
    Optional<Game> optionalGame =
        gameDBUtility.get(
            Filters.and(
                eq("timeControl", timeControl),
                eq("gameStatus", GameStatus.PENDING),
                gte("rating", rating - RATING_MARGIN),
                lte("rating", rating + RATING_MARGIN)));

    if (optionalGame.isEmpty()) {
      return Optional.empty();
    }

    return optionalGame;
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
}
