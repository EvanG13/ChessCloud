package org.example.models.responses.websocket;

import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@AllArgsConstructor
public class SocketMessageData {
  protected boolean isSuccess;
  protected String message;

  public SocketMessageData() {
    this.isSuccess = true;
    this.message = "Success";
  }
}
