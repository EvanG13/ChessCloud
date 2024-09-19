package org.example.handlers.rest;

import static org.example.utils.APIGatewayResponseBuilder.makeHttpResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import org.example.constants.StatusCodes;
import org.example.models.requests.RegisterRequest;
import org.example.services.RegisterService;
import org.example.utils.ValidateObject;

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
      return makeHttpResponse(
          StatusCodes.BAD_REQUEST, "Missing Event object or Event request body");
    }

    // Deserialize the request body into a LoginRequest object
    Gson gson = new Gson();
    RegisterRequest registerRequest = gson.fromJson(event.getBody(), RegisterRequest.class);
    try {
      ValidateObject.requireNonNull(registerRequest);
    } catch (NullPointerException e) {
      return makeHttpResponse(StatusCodes.BAD_REQUEST, "Missing argument(s)");
    }

    // TODO filter user credentials to meet standards
    if (service.doesEmailExist(registerRequest.email())) {
      return makeHttpResponse(StatusCodes.CONFLICT, "Email already exists");
    }

    service.registerUser(registerRequest);

    return makeHttpResponse(StatusCodes.OK, "Successfully registered");
  }
}
