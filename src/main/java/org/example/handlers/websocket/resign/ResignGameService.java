package org.example.handlers.websocket.resign;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.example.entities.game.Game;
import org.example.entities.game.GameDbService;
import org.example.enums.ResultReason;
import org.example.exceptions.InternalServerError;
import org.example.exceptions.NotFound;
import org.example.services.GameOverService;
import org.example.utils.socketMessenger.SocketMessenger;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResignGameService {
  @Builder.Default private final GameDbService gameDbService = new GameDbService();

  public void resign(String gameId, String resigningPlayerId, SocketMessenger messenger)
      throws NotFound, InternalServerError {
    Game game;
    try {
      game = gameDbService.get(gameId);
    } catch (NotFound e) {
      throw new NotFound("No Game found with id" + gameId);
    }

    GameOverService service =
        new GameOverService(ResultReason.FORFEIT, game, resigningPlayerId, messenger);

    service.endGame();
  }
}
