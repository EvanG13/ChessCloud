package org.example.models.websocketResponses;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class GameCreatedMessageData extends SocketMessageData {

  public GameCreatedMessageData() {
    super(true, "game created!");
  }
}
