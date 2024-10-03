package org.example.models.responses.websocket;

import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

@Getter
@SuperBuilder
public class MakeMoveMessageData extends SocketMessageData {
  private String fen;
  private List<String> moveList;
  private boolean isWhiteTurn;

  public MakeMoveMessageData(String fen, List<String> moveList, boolean isWhiteTurn) {
    super();
    this.fen = fen;
    this.moveList = moveList;
    this.isWhiteTurn = isWhiteTurn;
  }
}
