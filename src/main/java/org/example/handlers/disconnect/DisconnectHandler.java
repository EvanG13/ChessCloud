package org.example.handlers.disconnect;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import org.example.databases.ConnectionMongoDBUtility;
import org.example.statusCodes.StatusCodes;

public class DisconnectHandler
    implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {

  ConnectionMongoDBUtility utility;

  public DisconnectHandler() {
    this.utility = new ConnectionMongoDBUtility();
  }

  @Override
  public APIGatewayV2WebSocketResponse handleRequest(
      APIGatewayV2WebSocketEvent event, Context context) {
    String connectionId = event.getRequestContext().getConnectionId();

    utility.delete(connectionId);

    APIGatewayV2WebSocketResponse response = new APIGatewayV2WebSocketResponse();
    response.setStatusCode(StatusCodes.OK);

    return response;
  }
}
