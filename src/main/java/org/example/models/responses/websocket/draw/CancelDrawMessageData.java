package org.example.models.responses.websocket.draw;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.models.responses.websocket.SocketMessageData;

@Getter
@Setter
@SuperBuilder
public class CancelDrawMessageData extends SocketMessageData {
  public CancelDrawMessageData() {
    super(true, "Draw offer cancelled");
  }
}