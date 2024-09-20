package org.example.handlers.rest;

import static org.example.utils.APIGatewayResponseBuilder.makeHttpResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.example.constants.StatusCodes;
import org.example.exceptions.StatusCodeException;
import org.example.services.LogoutService;

public class LogoutHandler
    implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
  private final LogoutService service;

  public LogoutHandler() {
    service = new LogoutService();
  }

  public LogoutHandler(LogoutService service) {
    this.service = service;
  }

  @Override
  public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
    if (event == null || event.getHeaders() == null) {
      return makeHttpResponse(
          StatusCodes.BAD_REQUEST, "Missing Event object or Event Request Headers");
    }

    String sessionToken =
        event.getHeaders().get("Authorization").replace("Bearer ", "").replace("\"", "");
    String userId = event.getHeaders().get("userId");
    service.logout(sessionToken);
    try {
      service.handleUserInGame(userId);
    } catch (StatusCodeException e) {
      System.out.println(e.getMessage());
      return e
          .makeHttpResponse(); // TODO: since this can return a 500 status code, add that to the
                               // yaml
    }
    return makeHttpResponse(StatusCodes.OK, "Logged out successfully");
  }
}
