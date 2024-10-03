package org.example.handlers.rest;

import static org.example.utils.APIGatewayResponseBuilder.makeHttpResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import org.example.constants.StatusCodes;
import org.example.entities.game.Game;
import org.example.exceptions.NotFound;
import org.example.services.GameStateService;

public class GetGameStateHandler
    implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
  private final GameStateService service;

  public GetGameStateHandler() {
    service = new GameStateService();
  }

  public GetGameStateHandler(GameStateService service) {
    this.service = service;
  }

  @Override
  public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
    if (event == null || event.getHeaders() == null) {
      return makeHttpResponse(
          StatusCodes.BAD_REQUEST, "Missing Event object or Event Request Headers");
    }

    String userId = event.getHeaders().get("userid");
    Game game;
    try {
      game = service.getGameFromUserID(userId);
    } catch (NotFound e) {
      return e.makeHttpResponse();
    }

    return makeHttpResponse(StatusCodes.OK, game.toResponseJson());
  }
}
