package org.example.handlers.logout;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import org.example.statusCodes.StatusCodes;

public class LogoutHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent apiGatewayV2HTTPEvent, Context context) {

        return APIGatewayV2HTTPResponse.builder()
                .withBody(new LogoutService().getMessage())
                .withStatusCode(StatusCodes.OK)
                .build();
    }
}
