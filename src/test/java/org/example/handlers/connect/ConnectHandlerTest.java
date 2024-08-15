package org.example.handlers.connect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import java.util.UUID;
import org.example.statusCodes.StatusCodes;
import org.example.utils.FakeContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class ConnectHandlerTest {

  @DisplayName("OK ✅")
  @Test
  public void returnSuccess() {
    ConnectHandler connectHandler = new ConnectHandler();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    Context context = new FakeContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();

    String randomConnectionId = UUID.randomUUID().toString();

    requestContext.setConnectionId(randomConnectionId);

    event.setRequestContext(requestContext);

    APIGatewayV2WebSocketResponse response = connectHandler.handleRequest(event, context);
    assertEquals(response.getStatusCode(), StatusCodes.OK);
  }
}
