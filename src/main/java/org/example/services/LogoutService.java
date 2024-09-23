package org.example.services;

import lombok.AllArgsConstructor;
import org.example.enums.ResultReason;
import org.example.exceptions.InternalServerError;
import org.example.exceptions.NotFound;
import org.example.utils.GameOverUtility;

@AllArgsConstructor
public class LogoutService {
  private final SessionService service;
  private final GameStateService gameService;

  public LogoutService() {
    service = new SessionService();
    gameService = new GameStateService();
  }

  public void handleUserInGame(String userId) throws InternalServerError, NotFound {
    gameService.getGameFromUserID(userId);

    // the logging out user is in a game and therefore we make them forfeit:
    GameOverUtility gameOverUtility = new GameOverUtility(ResultReason.FORFEIT, userId);
    gameOverUtility.archiveGame();
    gameOverUtility.emitOutcome(); // can throw InternalServerError for now
    gameOverUtility.updateGame();
    gameOverUtility.updateRatings();
  }

  public void logout(String sessionToken) {
    service.delete(sessionToken);
  }
}
