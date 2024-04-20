package org.example.handlers.login;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import org.example.requestRecords.LoginRequest;
import org.example.statusCodes.StatusCodes;

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

    // Deserialize the request body into a LoginRequest object
    Gson gson = new Gson();
    LoginRequest loginRequest = gson.fromJson(event.getBody(), LoginRequest.class);

    if (!service.authenticateUser(loginRequest.email(), loginRequest.password())) {
      return APIGatewayV2HTTPResponse.builder()
          .withBody("Email or Password is incorrect")
          .withStatusCode(StatusCodes.UNAUTHORIZED)
          .build();
    }

    APIGatewayV2HTTPEvent.RequestContext.Authorizer.JWT jwt =
        new APIGatewayV2HTTPEvent.RequestContext.Authorizer.JWT();

    return APIGatewayV2HTTPResponse.builder()
        .withBody(jwt.toString())
        .withStatusCode(StatusCodes.OK)
        .build();
  }
}
