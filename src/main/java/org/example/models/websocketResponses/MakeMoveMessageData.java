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

  public MakeMoveMessageData(String fen, List<String> moveList) {
    super();
    this.fen = fen;
    this.moveList = moveList;
  }
}
