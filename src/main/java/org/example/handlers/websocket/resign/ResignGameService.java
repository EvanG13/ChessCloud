package org.example.handlers.websocket.resign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.example.entities.game.Game;
import org.example.entities.game.GameDbService;
import org.example.entities.player.Player;
import org.example.enums.ResultReason;
import org.example.exceptions.InternalServerError;
import org.example.exceptions.NotFound;
import org.example.exceptions.Unauthorized;
import org.example.services.GameOverService;
import org.example.utils.socketMessenger.SocketMessenger;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResignGameService {
  @Builder.Default private final GameDbService gameDbService = new GameDbService();

  public void resign(String gameId, String connectionId, SocketMessenger messenger)
      throws NotFound, InternalServerError, Unauthorized {
    Game game;
    try {
      game = gameDbService.get(gameId);
    } catch (NotFound e) {
      throw new NotFound("No Game found with id " + gameId);
    }

    Player resigningPlayer =
        game.getPlayers().stream()
            .filter(player -> player.getConnectionId().equals(connectionId))
            .findFirst()
            .orElseThrow(() -> new Unauthorized("Your connection ID is not bound to this game"));
    // or Forbidden (or something) because if not found among the two players, that should mean they
    // aren't in the game?

    GameOverService service =
        new GameOverService(ResultReason.FORFEIT, game, resigningPlayer.getPlayerId(), messenger);

    service.endGame();
  }
}
