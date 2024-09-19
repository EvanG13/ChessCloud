package org.example.handlers.rest;

import static org.example.utils.APIGatewayResponseBuilder.makeHttpResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import org.example.constants.StatusCodes;
import org.example.entities.User;
import org.example.exceptions.Unauthorized;
import org.example.models.requests.LoginRequest;
import org.example.models.requests.SessionRequest;
import org.example.models.responses.LoginResponseBody;
import org.example.services.LoginService;
import org.example.services.SessionService;
import org.example.utils.ValidateObject;

public class LoginHandler
    implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

  private final LoginService service;

  public LoginHandler() {
    service = new LoginService();
  }

  public LoginHandler(LoginService service) {
    this.service = service;
  }

  @Override
  public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
    if (event == null || event.getBody() == null) {
      return makeHttpResponse(StatusCodes.BAD_REQUEST, "no event/event.body");
    }

    LoginRequest loginRequestData = (new Gson()).fromJson(event.getBody(), LoginRequest.class);
    try {
      ValidateObject.requireNonNull(loginRequestData);
    } catch (NullPointerException e) {
      return makeHttpResponse(StatusCodes.BAD_REQUEST, "Missing argument(s)");
    }

    User user;
    try {
      user = service.authenticateUser(loginRequestData.email(), loginRequestData.password());
    } catch (Unauthorized e) {
      return e.makeHttpResponse();
    }

    SessionService sessionService = new SessionService();

    String sessionToken = sessionService.createSession(new SessionRequest(user.getId()));

    LoginResponseBody response = new LoginResponseBody(sessionToken, user);

    return makeHttpResponse(StatusCodes.OK, response.toJSON());
  }
}
