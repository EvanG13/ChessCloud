package org.example.handlers.login;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.example.entities.User;
import org.example.requestRecords.LoginRequest;
import org.example.statusCodes.StatusCodes;
import org.example.utils.JWTUtils;

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
          .withHeaders(new User().getCorsHeaders())
          .withBody("no event/event.body")
          .withStatusCode(StatusCodes.BAD_REQUEST)
          .build();
    }

    Gson gson = new Gson();
    LoginRequest loginRequest = gson.fromJson(event.getBody(), LoginRequest.class);

    User user = service.authenticateUser(loginRequest.email(), loginRequest.password());
    if (user == null) {
      return APIGatewayV2HTTPResponse.builder()
          .withHeaders(user.getCorsHeaders())
          .withBody("Email or Password is incorrect")
          .withStatusCode(StatusCodes.UNAUTHORIZED)
          .build();
    }

    JsonObject responseBody = new JsonObject();

    JWTUtils jwtUtils = new JWTUtils();

    responseBody.addProperty("jwt", jwtUtils.generateJWT(user.getEmail()));
    responseBody.addProperty("user", user.toResponseJson());

    return APIGatewayV2HTTPResponse.builder()
        .withHeaders(user.getCorsHeaders())
        .withBody(responseBody.toString())
        .withStatusCode(StatusCodes.OK)
        .build();
  }
}
