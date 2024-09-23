package org.example.models.responses;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import org.example.enums.ResultReason;
import org.example.exceptions.InternalServerError;

@Getter
public class GameOverResponseBody extends ResponseBody {
  @Expose private final ResultReason resultReason;
  @Expose private final String action = "GAME_ENDED";
  @Expose private final String displayMessage;
  @Expose private final String winnerUsername;
  @Expose private final String loserUsername;

  public GameOverResponseBody(ResultReason resultReason, String winnerUsername, String loserUsername) throws InternalServerError {
    this.resultReason = resultReason;
    this.winnerUsername = winnerUsername;
    this.loserUsername = loserUsername;

    switch (resultReason) {
      case ABORTED:
        this.displayMessage = String.format("Game invalid. Reason: %s", resultReason.getMessage());
        break;
      case FORFEIT:
      case TIMEOUT:
      case CHECKMATE:
        this.displayMessage = String.format("%s won! Reason: %s", winnerUsername, resultReason.getMessage());
        break;
      case REPETITION:
      case INSUFFICIENT_MATERIAL:
        this.displayMessage = String.format("Draw! Reason: %s", resultReason.getMessage());
        break;
      default:
        throw new InternalServerError("Unsupported ResultReason: " + resultReason);
    }
  }
}
