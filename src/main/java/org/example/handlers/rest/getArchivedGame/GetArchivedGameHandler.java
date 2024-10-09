package org.example.handlers.rest.getArchivedGame;

import static org.example.utils.APIGatewayResponseBuilder.makeHttpResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import java.util.Map;
import org.example.constants.StatusCodes;
import org.example.entities.game.ArchivedGame;
import org.example.exceptions.NotFound;

public class GetArchivedGameHandler
    implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

  private final GetArchivedGameService service;

  public GetArchivedGameHandler() {
    service = new GetArchivedGameService();
  }

  public GetArchivedGameHandler(GetArchivedGameService service) {
    this.service = service;
  }

  @Override
  public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
    Map<String, String> pathParams = event.getPathParameters();
    if (pathParams == null) {
      return makeHttpResponse(StatusCodes.BAD_REQUEST, "No path params");
    }

    String id = pathParams.get("gameId");
    if (id == null) {
      return makeHttpResponse(StatusCodes.BAD_REQUEST, "Missing gameId from path params");
    }

    ArchivedGame archivedGame;
    try {
      archivedGame = service.getArchivedGame(id);
    } catch (NotFound e) {
      return e.makeHttpResponse();
    }

    return makeHttpResponse(StatusCodes.OK, archivedGame.toResponseJson());
  }
}
