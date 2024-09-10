package org.example.handlers.login;

import static org.example.handlers.Responses.makeHttpResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.util.Optional;
import org.example.entities.User;
import org.example.handlers.session.SessionService;
import org.example.requestRecords.LoginRequest;
import org.example.requestRecords.SessionRequest;
import org.example.statusCodes.StatusCodes;
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

    Gson gson = new Gson();
    LoginRequest loginRequestData = gson.fromJson(event.getBody(), LoginRequest.class);
    try {
      ValidateObject.requireNonNull(loginRequestData);
    } catch (NullPointerException e) {
      return makeHttpResponse(StatusCodes.BAD_REQUEST, "Missing argument(s)");
    }

    Optional<User> optionalUser =
        service.authenticateUser(loginRequestData.email(), loginRequestData.password());
    if (optionalUser.isEmpty()) {
      return makeHttpResponse(StatusCodes.UNAUTHORIZED, "Email or Password is incorrect");
    }

    User user = optionalUser.get();

    JsonObject responseBody = new JsonObject();

    SessionService sessionService = new SessionService();

    String sessionToken = sessionService.createSession(new SessionRequest(user.getId()));

    responseBody.addProperty("token", sessionToken);
    responseBody.addProperty("user", user.toResponseJson());

    return makeHttpResponse(StatusCodes.OK, responseBody.toString());
  }
}
