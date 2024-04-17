package org.example.handlers.login;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import org.example.requestRecords.LoginRequest;
import org.example.statusCodes.StatusCodes;
import org.example.utils.EncryptPassword;

public class LoginHandler
    implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
  @Override
  public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
    if (event == null || event.getBody() == null) {
      return APIGatewayV2HTTPResponse.builder()
          .withBody("no event/event.body")
          .withStatusCode(StatusCodes.BAD_REQUEST)
          .build();
    }

    LambdaLogger logger = context.getLogger();

    // Deserialize the request body into a LoginRequest object
    Gson gson = new Gson();
    LoginRequest loginRequest;
    try {
      loginRequest = gson.fromJson(event.getBody(), LoginRequest.class);
    } catch (JsonSyntaxException e) {
      logger.log("Error deserializing JSON: " + e.getMessage());
      throw e;
    }

    String encryptedPassword = EncryptPassword.encrypt(loginRequest.password());

    LoginService service = new LoginService(loginRequest.email(), encryptedPassword);

    if (!service.authenticateUser()) {
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
