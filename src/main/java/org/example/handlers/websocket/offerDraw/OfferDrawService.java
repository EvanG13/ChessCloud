package org.example.handlers.websocket.offerDraw;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.example.entities.game.GameDbService;
import org.example.enums.Action;
import org.example.exceptions.NotFound;
import org.example.models.responses.websocket.OfferDrawMessageData;
import org.example.models.responses.websocket.SocketResponseBody;
import org.example.utils.socketMessenger.SocketEmitter;
import org.example.utils.socketMessenger.SocketMessenger;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OfferDrawService {
  @Builder.Default private final GameDbService gameDbService = new GameDbService();
  @Builder.Default private final SocketMessenger messenger = new SocketEmitter();

  public void offerDraw(String gameId, String playerOfferingDrawConnectionId) throws NotFound {
    String[] connectionIds = gameDbService.getConnectedIdsOffGame(gameId);

    String targetConnectionId;
    if (playerOfferingDrawConnectionId.equals(connectionIds[0])) {
      targetConnectionId = connectionIds[1];
    } else {
      targetConnectionId = connectionIds[0];
    }

    OfferDrawMessageData messageData = new OfferDrawMessageData();
    SocketResponseBody<OfferDrawMessageData> responseBody =
        new SocketResponseBody<>(Action.OFFER_DRAW, messageData);
    messenger.sendMessage(targetConnectionId, responseBody.toJSON());
  }
}
