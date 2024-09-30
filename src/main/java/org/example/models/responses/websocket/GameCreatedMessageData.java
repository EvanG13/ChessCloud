package org.example.models.responses.websocket;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class GameCreatedMessageData extends SocketMessageData {

  public GameCreatedMessageData() {
    super(true, "game created!");
  }
}
