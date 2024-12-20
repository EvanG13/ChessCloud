package org.example.handlers.rest.logout;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.example.entities.session.SessionUtility;
import org.example.enums.ResultReason;
import org.example.exceptions.InternalServerError;
import org.example.exceptions.NotFound;
import org.example.handlers.rest.getGameState.GameStateService;
import org.example.handlers.websocket.gameOver.GameOverService;
import org.example.utils.socketMessenger.SocketEmitter;
import org.example.utils.socketMessenger.SocketMessenger;

@AllArgsConstructor
@Builder
@NoArgsConstructor
public class LogoutService {
  private final @Builder.Default SessionUtility sessionUtility = new SessionUtility();
  private final @Builder.Default GameStateService gameService = new GameStateService();
  private final @Builder.Default SocketMessenger socketMessenger = new SocketEmitter();

  public void handleUserInGame(String userId) throws InternalServerError {
    GameOverService gameOverService;
    try {
      gameOverService = new GameOverService(ResultReason.FORFEIT, userId, socketMessenger);
      gameOverService.endGame();
    } catch (NotFound ignored) {
    }
  }

  public void logout(String sessionToken) {
    sessionUtility.delete(sessionToken);
  }
}
