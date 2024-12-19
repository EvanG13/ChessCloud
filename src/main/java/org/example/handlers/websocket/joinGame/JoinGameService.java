package org.example.handlers.websocket.joinGame;

import static com.mongodb.client.model.Filters.*;

import com.mongodb.client.model.Filters;
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
import org.example.exceptions.InternalServerError;
import org.example.exceptions.NotFound;
import org.example.exceptions.Unauthorized;

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

  public Game getPendingGame(GameMode gameMode, TimeControl timeControl, int rating)
      throws NotFound {
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

  public User getUser(String userId) throws Unauthorized {
    return userUtility.get(userId).orElseThrow(() -> new Unauthorized("No user matches userId"));
  }

  public Stats getUserStats(String userId) throws InternalServerError {
    return statsUtility
        .get(userId)
        .orElseThrow(() -> new InternalServerError("User doesn't have entry in Stats collection"));
  }

  public GameMode determineGameMode(TimeControl timeControl) throws NotFound {
    return GameMode.fromTime(timeControl.getBase());
  }
}
