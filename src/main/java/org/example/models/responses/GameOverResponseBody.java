package org.example.models.responses;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import org.example.enums.ResultReason;
import org.example.exceptions.BadRequest;

@Getter
public class GameOverResponseBody extends ResponseBody {
  @Expose private final ResultReason resultReason;
  @Expose private final String action = "GAME_ENDED";
  @Expose private final String displayMessage;
  @Expose private final String winnerUsername;
  @Expose private final String loserUsername;

  public GameOverResponseBody(
      ResultReason resultReason, String winnerUsername, String loserUsername) throws BadRequest {
    this.resultReason = resultReason;
    this.winnerUsername = winnerUsername;
    this.loserUsername = loserUsername;
    this.displayMessage = resultReason.getMessage(winnerUsername);
  }
}
