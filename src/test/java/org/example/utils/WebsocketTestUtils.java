package org.example.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public class WebsocketTestUtils {
  private static final Gson gson = new Gson();

  public static APIGatewayV2WebSocketEvent.RequestContext makeRoutelessRequestContext(
      String connectionId) {
    return makeRequestContext("", connectionId, System.currentTimeMillis());
  }

  public static APIGatewayV2WebSocketEvent.RequestContext makeRequestContext(
      String route, String connectionId) {
    return makeRequestContext(route, connectionId, System.currentTimeMillis());
  }

  public static APIGatewayV2WebSocketEvent.RequestContext makeRequestContext(
      String route, String connectionId, long timeEpoch) {
    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setRouteKey(route);
    requestContext.setConnectionId(connectionId);
    requestContext.setRequestTimeEpoch(timeEpoch);
    return requestContext;
  }

  public static APIGatewayV2WebSocketEvent makeEvent(
      String body, APIGatewayV2WebSocketEvent.RequestContext requestContext) {
    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
    event.setBody(body);
    event.setRequestContext(requestContext);
    return event;
  }

  public static APIGatewayV2WebSocketEvent makeEvent(
      String body,
      Map<String, String> queryStrings,
      APIGatewayV2WebSocketEvent.RequestContext requestContext) {
    APIGatewayV2WebSocketEvent event = makeEvent(body, requestContext);
    event.setQueryStringParameters(queryStrings);
    return event;
  }

  public static APIGatewayV2WebSocketResponse getResponse(
      RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> handler,
      APIGatewayV2WebSocketEvent event) {
    return handler.handleRequest(event, new MockContext());
  }

  public static APIGatewayV2WebSocketResponse getResponse(
      RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> handler,
      String body,
      APIGatewayV2WebSocketEvent.RequestContext requestContext) {
    return getResponse(handler, makeEvent(body, requestContext));
  }

  public static APIGatewayV2WebSocketResponse getResponse(
      RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> handler,
      String body,
      Map<String, String> queryStrings,
      APIGatewayV2WebSocketEvent.RequestContext requestContext) {
    return getResponse(handler, makeEvent(body, queryStrings, requestContext));
  }

  public static APIGatewayV2WebSocketResponse getResponse(
      RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> handler,
      Object body,
      APIGatewayV2WebSocketEvent.RequestContext requestContext) {
    return getResponse(handler, makeEvent(gson.toJson(body), requestContext));
  }

  public static APIGatewayV2WebSocketResponse getResponse(
      RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> handler,
      Object body,
      Map<String, String> queryStrings,
      APIGatewayV2WebSocketEvent.RequestContext requestContext) {
    return getResponse(handler, makeEvent(gson.toJson(body), queryStrings, requestContext));
  }

  public static void assertResponse(
      APIGatewayV2WebSocketResponse response, int expectedStatusCode, String expectedBody) {
    assertEquals(
        expectedStatusCode, response.getStatusCode(), "Status code does not match expected value");
    assertEquals(expectedBody, response.getBody(), "Response body does not match expected value");
  }
}
