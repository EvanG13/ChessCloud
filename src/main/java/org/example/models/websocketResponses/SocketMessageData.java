package org.example.models.websocketResponses;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@AllArgsConstructor
public class SocketMessageData {
  @Expose protected boolean isSuccess;
  @Expose protected String message;

  public SocketMessageData() {
    this.isSuccess = true;
    this.message = "Success";
  }
}
