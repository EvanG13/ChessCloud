package org.example.handlers.register;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import com.mongodb.MongoException;
import org.example.requestRecords.RegisterRequest;
import org.example.statusCodes.StatusCodes;
import org.example.utils.AuthHeaders;

public class RegisterHandler
    implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

  private final RegisterService service;

  public RegisterHandler() {
    service = new RegisterService();
  }

  public RegisterHandler(RegisterService service) {
    this.service = service;
  }

  @Override
  public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
    if (event == null || event.getBody() == null) {
      return APIGatewayV2HTTPResponse.builder()
          .withBody("Missing Event object or Event request body")
          .withStatusCode(StatusCodes.BAD_REQUEST)
          .build();
    }

    // Deserialize the request body into a LoginRequest object
    Gson gson = new Gson();
    RegisterRequest registerRequest = gson.fromJson(event.getBody(), RegisterRequest.class);

    // TODO filter user credentials to meet standards

    if (service.doesEmailExist(registerRequest.email())) {
      return APIGatewayV2HTTPResponse.builder()
          .withBody("Email already exists")
          .withStatusCode(StatusCodes.CONFLICT)
          .build();
    }

    try {
      service.registerUser(
          registerRequest.email(), registerRequest.username(), registerRequest.password());
    } catch (MongoException e) {
      LambdaLogger logger = context.getLogger();
      logger.log(e.getMessage());
      throw e;
    }

    return APIGatewayV2HTTPResponse.builder()
        .withHeaders(AuthHeaders.getCorsHeaders())
        .withBody("Successfully registered")
        .withStatusCode(StatusCodes.OK)
        .build();
  }
}
