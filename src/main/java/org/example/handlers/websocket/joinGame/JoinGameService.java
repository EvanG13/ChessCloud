package org.example.handlers.websocket.joinGame;

import static com.mongodb.client.model.Filters.*;

import com.mongodb.client.model.Filters;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.example.constants.ChessConstants;
import org.example.entities.game.Game;
import org.example.entities.game.GameUtility;
import org.example.entities.stats.Stats;
import org.example.entities.stats.StatsUtility;
import org.example.entities.timeControl.TimeControl;
import org.example.entities.user.User;
import org.example.entities.user.UserUtility;
import org.example.enums.GameMode;
import org.example.enums.GameStatus;
import org.example.exceptions.NotFound;

@AllArgsConstructor
public class JoinGameService {
  private final GameUtility gameUtility;
  private final UserUtility userUtility;
  private final StatsUtility statsUtility;

  public JoinGameService() {
    this.gameUtility = new GameUtility();
    this.userUtility = new UserUtility();
    this.statsUtility = new StatsUtility();
  }

  public Game getPendingGame(GameMode gameMode, TimeControl timeControl, int rating) throws NotFound {
    return gameUtility.getGame(
        and(
            eq("gameMode", gameMode),
            eq("timeControl.base", timeControl.getBase()),
            eq("timeControl.increment", timeControl.getIncrement()),
            eq("gameStatus", GameStatus.PENDING),
            gte("rating", rating - ChessConstants.RATING_MARGIN),
            lte("rating", rating + ChessConstants.RATING_MARGIN)));
  }

  public boolean isInGame(String userId) {
    try {
      gameUtility.getGame(elemMatch("players", Filters.eq("playerId", userId)));
    } catch (NotFound e) {
      return false;
    }

    return true;
  }

  public void createGame(Game game) {
    gameUtility.post(game);
  }

  public void updateGame(Game game) {
    gameUtility.put(game.getId(), game);
  }

  public Optional<User> getUser(String userId) {
    Optional<User> user = userUtility.get(userId);
    if (user.isEmpty()) {
      return Optional.empty();
    }
    return user;
  }

  public Optional<Stats> getUserStats(String userId) {
    Optional<Stats> stats = statsUtility.get(userId);
    if (stats.isEmpty()) {
      return Optional.empty();
    }
    return stats;
  }

  public GameMode determineGameMode(TimeControl timeControl) throws NotFound {
    return GameMode.fromTime(timeControl.getBase());
  }
}
