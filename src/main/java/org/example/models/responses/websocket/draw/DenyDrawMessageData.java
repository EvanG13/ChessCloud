package org.example.models.responses.websocket.draw;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.models.responses.websocket.SocketMessageData;

@Getter
@Setter
@SuperBuilder
public class DenyDrawMessageData extends SocketMessageData {
  public DenyDrawMessageData() {
    super(true, "Draw offer denied");
  }
}