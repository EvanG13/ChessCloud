package org.example.models.responses.websocket;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class ChatMessageData extends SocketMessageData {
  private String chatMessage;

  public ChatMessageData(String chatMessage) {
    super();
    this.chatMessage = chatMessage;
  }
}
