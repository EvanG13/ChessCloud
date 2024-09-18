package org.example.handlers.getGameState;

import static org.example.handlers.Responses.makeHttpResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import java.util.Optional;
import org.example.entities.Game;
import org.example.statusCodes.StatusCodes;

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
    Optional<Game> optionalGame = service.getGame(userId);

    if (optionalGame.isEmpty()) return makeHttpResponse(StatusCodes.NOT_FOUND, "No game found");
    Game game = optionalGame.get();
    return makeHttpResponse(StatusCodes.OK, game.toResponseJson());
  }
}
