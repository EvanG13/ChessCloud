package org.example.handlers.logout;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import org.example.handlers.session.SessionService;
import org.example.requestRecords.LogoutRequest;
import org.example.statusCodes.StatusCodes;

public class LogoutHandler
    implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
  private LogoutService service;

  public LogoutHandler() {
    service = new LogoutService();
  }

  @Override
  public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
    Gson gson = new Gson();
    String sessionToken = gson.fromJson(event.getBody(), LogoutRequest.class).sessionToken();
    service.destroySession(sessionToken);
    return APIGatewayV2HTTPResponse.builder()
        .withBody(new LogoutService().getMessage())
        .withStatusCode(StatusCodes.OK)
        .build();
  }
}
