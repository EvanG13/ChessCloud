package org.example.services;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.example.entities.session.SessionDbService;
import org.example.enums.ResultReason;
import org.example.exceptions.InternalServerError;
import org.example.exceptions.NotFound;
import org.example.utils.socketMessenger.SocketEmitter;
import org.example.utils.socketMessenger.SocketMessenger;

@AllArgsConstructor
@Builder
@NoArgsConstructor
public class LogoutService {
  private final @Builder.Default SessionDbService sessionDbService = new SessionDbService();
  private final @Builder.Default GameStateService gameService = new GameStateService();
  private final @Builder.Default SocketMessenger socketMessenger = new SocketEmitter();

  public void handleUserInGame(String userId) throws InternalServerError {
    GameOverService gameOverService;
    try {
      gameOverService = new GameOverService(ResultReason.FORFEIT, userId, socketMessenger);
    } catch (NotFound e) {
      return;
    }
  }

  public void logout(String sessionToken) {
    sessionDbService.delete(sessionToken);
  }
}
