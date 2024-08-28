package org.example.handlers.logout;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import org.example.requestRecords.LogoutRequest;
import org.example.statusCodes.StatusCodes;
import org.example.utils.AuthHeaders;

public class LogoutHandler
    implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
  private LogoutService service;

  public LogoutHandler() {
    service = new LogoutService();
  }

  public LogoutHandler(LogoutService service) {
    this.service = service;
  }

  @Override
  public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
    if (event == null || event.getBody() == null) {
      return APIGatewayV2HTTPResponse.builder()
              .withHeaders(AuthHeaders.getCorsHeaders())
              .withBody("Missing Event object or Event request body")
              .withStatusCode(StatusCodes.BAD_REQUEST)
              .build();
    }
    Gson gson = new Gson();
    String sessionToken = gson.fromJson(event.getBody(), LogoutRequest.class).sessionToken();
    service.destroySession(sessionToken);
    return APIGatewayV2HTTPResponse.builder()
        .withBody(new LogoutService().getMessage())
        .withStatusCode(StatusCodes.OK)
        .build();
  }
}
