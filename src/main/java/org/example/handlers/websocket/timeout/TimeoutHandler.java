package org.example.handlers.websocket.timeout;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
import org.example.constants.StatusCodes;
import org.example.exceptions.StatusCodeException;
import org.example.handlers.websocket.resign.ResignGameService;
import org.example.models.requests.ResignRequest;
import org.example.models.requests.TimeoutRequest;
import org.example.utils.ValidateObject;
import org.example.utils.socketMessenger.SocketEmitter;
import org.example.utils.socketMessenger.SocketMessenger;

import static org.example.utils.APIGatewayResponseBuilder.makeWebsocketResponse;


public class TimeoutHandler
        implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {

    private final TimeoutService timeoutService;
    private final SocketMessenger messenger;

    public TimeoutHandler() {
        timeoutService = new TimeoutService();
        messenger = new SocketEmitter();
    }

    public TimeoutHandler(TimeoutService timeoutService, SocketMessenger messenger) {
        this.timeoutService = timeoutService;
        this.messenger = messenger;
    }

    @Override
    public APIGatewayV2WebSocketResponse handleRequest(
            APIGatewayV2WebSocketEvent event, Context context) {

        String connectionId = event.getRequestContext().getConnectionId();

        TimeoutRequest request = (new Gson()).fromJson(event.getBody(), TimeoutRequest.class);
        try {
            ValidateObject.requireNonNull(request);
        } catch (NullPointerException e) {
            return makeWebsocketResponse(StatusCodes.BAD_REQUEST, "Missing argument(s)");
        }

        try {
           timeoutService.processTimeout(request.gameId(), connectionId, messenger);
        } catch (StatusCodeException e) {
            System.out.println(e.getMessage());
            return e.makeWebsocketResponse();
        }

        return makeWebsocketResponse(StatusCodes.OK, "Successfully Registered Timeout!");
    }
}

