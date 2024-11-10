package org.example.handlers.rest.logout;

import static org.example.utils.APIGatewayResponseBuilder.makeHttpResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import java.util.Map;
import org.example.constants.StatusCodes;
import org.example.exceptions.InternalServerError;

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

    Map<String, String> headers = event.getHeaders();

    String userId = headers.get("userid");

    String sessionToken = headers.get("Authorization").replace("Bearer ", "").replace("\"", "");
    service.logout(sessionToken);

    try {
      service.handleUserInGame(userId);
    } catch (InternalServerError e) {
      return e.makeHttpResponse();
    }

    return makeHttpResponse(StatusCodes.OK, "Logged out successfully");
  }
}
