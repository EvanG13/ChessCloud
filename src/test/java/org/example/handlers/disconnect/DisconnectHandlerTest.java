package org.example.handlers.disconnect;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import org.example.statusCodes.StatusCodes;
import org.example.utils.FakeContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class DisconnectHandlerTest {

  @DisplayName("OK ✅")
  @Test
  public void returnSuccess() {
    DisconnectHandler disconnectHandler = new DisconnectHandler();

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    Context context = new FakeContext();

    APIGatewayV2WebSocketResponse response = disconnectHandler.handleRequest(event, context);
    assertEquals(response.getStatusCode(), StatusCodes.OK);
  }
}
