package org.example.handlers.logout;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.example.statusCodes.StatusCodes;
import org.example.utils.AuthHeaders;

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
      return APIGatewayV2HTTPResponse.builder()
          .withHeaders(AuthHeaders.getCorsHeaders())
          .withBody("Missing Event object or Event Request Headers")
          .withStatusCode(StatusCodes.BAD_REQUEST)
          .build();
    }

    String sessionToken =
        event.getHeaders().get("Authorization").replace("Bearer ", "").replace("\"", "");

    service.logout(sessionToken);

    return APIGatewayV2HTTPResponse.builder()
        .withBody("Logged out successfully.")
        .withStatusCode(StatusCodes.OK)
        .withHeaders(AuthHeaders.getCorsHeaders())
        .build();
  }
}
