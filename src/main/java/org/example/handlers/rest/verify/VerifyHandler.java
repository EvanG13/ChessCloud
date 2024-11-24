package org.example.handlers.rest.verify;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import org.example.constants.StatusCodes;
import org.example.exceptions.InternalServerError;
import org.example.exceptions.NotFound;
import org.example.models.requests.VerifyRequest;

import static org.example.utils.APIGatewayResponseBuilder.makeHttpResponse;

public class VerifyHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
  private final VerifyService verifyService;

  public VerifyHandler() {
    this.verifyService = new VerifyService();
  }

  public VerifyHandler(VerifyService verifyService) {
    this.verifyService = verifyService;
  }

  @Override
  public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
    if (event == null || event.getHeaders() == null) {
      return makeHttpResponse(
          StatusCodes.BAD_REQUEST, "Missing Event object or Event Request Headers");
    }

    Gson gson = new Gson();
    VerifyRequest verifyRequest = gson.fromJson(event.getBody(), VerifyRequest.class);

    try {
      verifyService.verify(verifyRequest.token());
    }
    catch (InternalServerError | NotFound e) {
      return e.makeHttpResponse();
    }

    return makeHttpResponse(StatusCodes.OK, "Email verified successfully");
  }
}
