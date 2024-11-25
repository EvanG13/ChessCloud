package org.example.handlers.rest.resetPassword;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import org.example.constants.StatusCodes;
import org.example.exceptions.InternalServerError;
import org.example.exceptions.NotFound;
import org.example.models.requests.ResetPasswordRequest;

import static org.example.utils.APIGatewayResponseBuilder.makeHttpResponse;

public class ResetPasswordHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
  private final ResetPasswordService resetPasswordService;

  public ResetPasswordHandler() {
    this.resetPasswordService = new ResetPasswordService();
  }

  public ResetPasswordHandler(ResetPasswordService resetPasswordService) {
    this.resetPasswordService = resetPasswordService;
  }

  @Override
  public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
    if (event == null || event.getHeaders() == null) {
      return makeHttpResponse(
          StatusCodes.BAD_REQUEST, "Missing Event object or Event Request Headers");
    }

    Gson gson = new Gson();
    ResetPasswordRequest resetPasswordRequest = gson.fromJson(event.getBody(), ResetPasswordRequest.class);

    try {
      resetPasswordService.resetPassword(resetPasswordRequest.token(), resetPasswordRequest.newPassword());
    }
    catch (InternalServerError | NotFound e) {
      return e.makeHttpResponse();
    }

    return makeHttpResponse(StatusCodes.OK, "Password successfully reset");
  }
}
