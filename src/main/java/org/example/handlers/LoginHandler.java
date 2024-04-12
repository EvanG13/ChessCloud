package org.example.handlers;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;

import com.google.gson.Gson;
import org.example.requests.LoginRequest;
import org.example.services.LoginService;
import org.example.statusCodes.StatusCodes;

public class LoginHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        if (event == null) {
            return APIGatewayV2HTTPResponse.builder()
                    .withBody("Bad Request")
                    .withStatusCode(StatusCodes.BAD_REQUEST)
                    .build();
        }

        String requestBody = event.getBody();

        LoginRequest request = new Gson().fromJson(requestBody, LoginRequest.class);

        LoginService service = new LoginService(request.getEmail(), request.getPassword());

        return APIGatewayV2HTTPResponse.builder()
                .withBody(service.getResponseMessage())
                .withStatusCode(StatusCodes.OK)
                .build();
    }
}
