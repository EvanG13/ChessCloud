package org.example.models.responses.websocket;

import java.util.List;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.example.entities.move.Move;

@Getter
@SuperBuilder
public class MakeMoveMessageData extends SocketMessageData {
  private String fen;
  private List<Move> moveList;
  private boolean isWhiteTurn;

  public MakeMoveMessageData(String fen, List<Move> moveList, boolean isWhiteTurn) {
    super();
    this.fen = fen;
    this.moveList = moveList;
    this.isWhiteTurn = isWhiteTurn;
  }
}
