package org.example.models.websocketResponses;

import com.google.gson.annotations.Expose;
import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class MakeMoveMessageData extends SocketMessageData {
  @Expose private String fen;
  @Expose private List<String> moveList;
  @Expose private boolean isWhiteTurn;

  public MakeMoveMessageData(String fen, List<String> moveList, boolean isWhiteTurn) {
    super();
    this.fen = fen;
    this.moveList = moveList;
    this.isWhiteTurn = isWhiteTurn;
  }
}
