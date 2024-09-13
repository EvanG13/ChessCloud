package org.example.handlers.logout;

import static org.example.handlers.Responses.makeHttpResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.example.statusCodes.StatusCodes;

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

    service.logout(sessionToken);

    return makeHttpResponse(StatusCodes.OK, "Logged out successfully");
  }
}
