package org.example.utils;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class APIGatewayResponseBuilder {

  public static final Map<String, String> corsHeaders =
      Map.of(
          "Access-Control-Allow-Origin", "*",
          "Access-Control-Allow-Headers", "*",
          "Access-Control-Allow-Methods", "POST,OPTIONS");

  public static APIGatewayV2HTTPResponse makeHttpResponse(int statusCode, String body) {
    return APIGatewayV2HTTPResponse.builder()
        .withStatusCode(statusCode)
        .withHeaders(corsHeaders)
        .withBody(body)
        .build();
  }

  public static APIGatewayV2WebSocketResponse makeWebsocketResponse(int statusCode, String body) {
    APIGatewayV2WebSocketResponse response = new APIGatewayV2WebSocketResponse();
    response.setStatusCode(statusCode);
    response.setBody(body);

    return response;
  }
}
