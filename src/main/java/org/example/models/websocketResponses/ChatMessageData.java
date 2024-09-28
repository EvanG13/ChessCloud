package org.example.models.websocketResponses;

import com.google.gson.annotations.Expose;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class ChatMessageData extends SocketMessageData {
  @Expose private String chatMessage;

  public ChatMessageData(boolean isSuccess, String message, String chatMessage) {
    super(isSuccess, message);
    this.chatMessage = chatMessage;
  }

  public ChatMessageData(String chatMessage) {
    super();
    this.chatMessage = chatMessage;
  }
}
