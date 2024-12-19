package org.example.exceptions;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import org.example.utils.APIGatewayResponseBuilder;

public abstract class StatusCodeException extends Exception {
  private final int statusCode;
  private final String message;

  public StatusCodeException(int statusCode, String message) {
    this.statusCode = statusCode;
    this.message = message;
  }

  public APIGatewayV2HTTPResponse makeHttpResponse() {
    return APIGatewayResponseBuilder.makeHttpResponse(statusCode, message);
  }

  public APIGatewayV2WebSocketResponse makeWebsocketResponse() {
    return APIGatewayResponseBuilder.makeWebsocketResponse(statusCode, message);
  }

  @Override
  public String getMessage() {
    return message;
  }
}
