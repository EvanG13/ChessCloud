package org.example.models.responses.websocket;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.example.enums.ResultReason;
import org.example.exceptions.InternalServerError;

@Getter
@SuperBuilder
public class GameOverMessageData extends SocketMessageData {
  private final ResultReason resultReason;
  private final String displayMessage;
  private final String winnerUsername;
  private final String loserUsername;

  public GameOverMessageData(ResultReason resultReason, String winnerUsername, String loserUsername)
      throws InternalServerError {
    super(true, "game ended");
    this.resultReason = resultReason;
    this.winnerUsername = winnerUsername;
    this.loserUsername = loserUsername;

    switch (resultReason) {
      case ABORTED ->
          this.displayMessage =
              String.format("Game invalid. Reason: %s", resultReason.getMessage());
      case FORFEIT, TIMEOUT, CHECKMATE ->
          this.displayMessage =
              String.format("%s won! Reason: %s", winnerUsername, resultReason.getMessage());
      case MUTUAL_DRAW, REPETITION, INSUFFICIENT_MATERIAL, STALEMATE, FIFTY_MOVE_RULE ->
          this.displayMessage = String.format("Draw! Reason: %s", resultReason.getMessage());
      default -> throw new InternalServerError("Unsupported ResultReason: " + resultReason);
    }
  }
}
