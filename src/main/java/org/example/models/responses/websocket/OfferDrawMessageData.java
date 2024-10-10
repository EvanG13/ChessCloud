package org.example.models.responses.websocket;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
public class OfferDrawMessageData extends SocketMessageData {
  public OfferDrawMessageData() {
    super(true, "Offering draw");
  }
}
