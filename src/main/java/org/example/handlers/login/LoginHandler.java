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

  @Override
  public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
    if (event == null || event.getBody() == null) {
      return APIGatewayV2HTTPResponse.builder()
          .withBody("Bad Request")
          .withStatusCode(StatusCodes.BAD_REQUEST)
          .build();
    }

    String requestBody = event.getBody();

    // Deserialize the request body into a LoginRequest object
    Gson gson = new Gson();
    LoginRequest loginRequest = gson.fromJson(requestBody, LoginRequest.class);

    // Check if loginRequest is null or any required field is missing
    if (loginRequest == null || loginRequest.email() == null || loginRequest.password() == null) {
      return APIGatewayV2HTTPResponse.builder()
          .withBody("GSON not working for whatever reason")
          .withStatusCode(StatusCodes.BAD_REQUEST)
          .build();
    }

    LoginService service = new LoginService(loginRequest.email(), loginRequest.password());

    return APIGatewayV2HTTPResponse.builder()
        .withBody(service.getResponseMessage())
        .withStatusCode(StatusCodes.OK)
        .build();
  }
}
