package org.example.handlers.joinGame;

import com.mongodb.client.model.Filters;
import java.util.Optional;
import org.example.databases.MongoDBUtility;
import org.example.entities.Game;
import org.example.entities.User;
import org.example.utils.GameStatus;
import org.example.utils.TimeControl;

public class JoinGameService {
  private final MongoDBUtility<Game> dbUtility;
  private final MongoDBUtility<User> userUtility;

  public JoinGameService(MongoDBUtility<Game> dbUtility, MongoDBUtility<User> userUtility) {
    this.dbUtility = dbUtility;
    this.userUtility = userUtility;
  }

  public JoinGameService() {
    this.dbUtility = new MongoDBUtility<>("games", Game.class);
    this.userUtility = new MongoDBUtility<>("users", User.class);
  }

  public Optional<Game> getPendingGame(TimeControl timeControl) {
    Optional<Game> optionalGame =
        dbUtility.get(
            Filters.and(
                Filters.eq("timeControl", timeControl),
                Filters.eq("gameStatus", GameStatus.PENDING)));

    if (optionalGame.isEmpty()) {
      return Optional.empty();
    }
    return optionalGame;
  }

  public void createGame(Game game) {
    dbUtility.post(game);
  }

  public void updateGame(Game game) {
    dbUtility.put(game.getId(), game);
  }

  public Optional<User> getUser(String userId) {
    Optional<User> user = userUtility.get(userId);
    if (user.isEmpty()) {
      return Optional.empty();
    }
    return user;
  }
}
