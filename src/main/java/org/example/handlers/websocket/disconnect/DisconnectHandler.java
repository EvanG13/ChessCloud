package org.example.handlers.websocket.disconnect;

import static org.example.utils.APIGatewayResponseBuilder.makeWebsocketResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import org.example.constants.StatusCodes;
import org.example.entities.connection.ConnectionUtility;

public class DisconnectHandler
    implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {

  private final ConnectionUtility connectionUtility;

  public DisconnectHandler() {
    this.connectionUtility = new ConnectionUtility();
  }

  public DisconnectHandler(ConnectionUtility connectionUtility) {
    this.connectionUtility = connectionUtility;
  }

  @Override
  public APIGatewayV2WebSocketResponse handleRequest(
      APIGatewayV2WebSocketEvent event, Context context) {
    String connectionId = event.getRequestContext().getConnectionId();

    connectionUtility.delete(connectionId);

    return makeWebsocketResponse(StatusCodes.OK, "Successfully disconnected");
  }
}
