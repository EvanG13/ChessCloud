package org.example.handlers.rest.resetPassword;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.google.gson.Gson;
import lombok.NoArgsConstructor;
import org.example.constants.StatusCodes;
import org.example.entities.token.PasswordResetToken;
import org.example.models.requests.RequestPasswordResetRequest;
import org.example.utils.ResendUtil;

import static org.example.utils.APIGatewayResponseBuilder.makeHttpResponse;

public class RequestPasswordResetHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

  private final RequestPasswordResetService requestPasswordResetService;

  public RequestPasswordResetHandler() {
    this.requestPasswordResetService = new RequestPasswordResetService();
  }

  public RequestPasswordResetHandler(RequestPasswordResetService requestPasswordResetService) {
    this.requestPasswordResetService = requestPasswordResetService;
  }

  @Override
  public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
    if (event == null || event.getHeaders() == null) {
      return makeHttpResponse(
          StatusCodes.BAD_REQUEST, "Missing Event object or Event Request Headers");
    }

    Gson gson = new Gson();
    RequestPasswordResetRequest requestPasswordResetRequest = gson.fromJson(event.getBody(), RequestPasswordResetRequest.class);

    requestPasswordResetService.sendEmailIfRegistered(requestPasswordResetRequest.email());

    return makeHttpResponse(StatusCodes.OK, "If the email exists, a link to reset the password has been sent");
  }
}
