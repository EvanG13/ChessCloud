package org.example.handlers.login;

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
import org.example.statusCodes.StatusCodes;
import org.example.utils.AuthHeaders;

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
      return APIGatewayV2HTTPResponse.builder()
          .withBody("no event/event.body")
          .withStatusCode(StatusCodes.BAD_REQUEST)
          .build();
    }

    Gson gson = new Gson();
    LoginRequest loginRequestData = gson.fromJson(event.getBody(), LoginRequest.class);

    Optional<User> optionalUser =
        service.authenticateUser(loginRequestData.email(), loginRequestData.password());
    if (optionalUser.isEmpty()) {
      return APIGatewayV2HTTPResponse.builder()
          .withBody("Email or Password is incorrect")
          .withStatusCode(StatusCodes.UNAUTHORIZED)
          .build();
    }

    User user = optionalUser.get();

    JsonObject responseBody = new JsonObject();

    SessionService sessionService = new SessionService();
    String token = sessionService.createSession(user.getId());

    responseBody.addProperty("sessionToken", token);
    responseBody.addProperty("user", user.toResponseJson());

    return APIGatewayV2HTTPResponse.builder()
        .withHeaders(AuthHeaders.getCorsHeaders())
        .withBody(responseBody.toString())
        .withStatusCode(StatusCodes.OK)
        .build();
  }
}
