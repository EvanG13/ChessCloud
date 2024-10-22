package org.example.handlers.rest.getArchivedGame;

import static org.example.utils.APIGatewayResponseBuilder.makeHttpResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import java.util.List;
import java.util.Map;
import org.example.constants.StatusCodes;
import org.example.entities.game.ArchivedGame;
import org.example.enums.TimeControl;
import org.example.models.responses.rest.ListArchivedGamesResponse;

public class ListArchivedGamesHandler
    implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

  private final GetArchivedGameService service;

  public ListArchivedGamesHandler() {
    service = new GetArchivedGameService();
  }

  public ListArchivedGamesHandler(GetArchivedGameService service) {
    this.service = service;
  }

  @Override
  public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
    Map<String, String> pathParams = event.getPathParameters();
    Map<String, String> queryMap = event.getQueryStringParameters();
    String username = pathParams != null ? pathParams.get("username") : null;

    if (username == null) {
      return makeHttpResponse(StatusCodes.BAD_REQUEST, "Bad path param. Expected username");
    }

    TimeControl timeControl = null;

    if (queryMap != null && !queryMap.isEmpty()) {
      if (!queryMap.containsKey("timeControl")) {

        return makeHttpResponse(
            StatusCodes.BAD_REQUEST, "Bad query param. Expected either none or timeControl");
      }
      try {
        timeControl = TimeControl.valueOf(queryMap.get("timeControl"));
      } catch (IllegalArgumentException e) {
        return makeHttpResponse(
            StatusCodes.BAD_REQUEST, "unsupported time control: " + queryMap.get("timeControl"));
      }
    }

    List<ArchivedGame> archivedGames = service.getArchivedGames(username, timeControl);
    ListArchivedGamesResponse res = new ListArchivedGamesResponse(archivedGames);
    return makeHttpResponse(StatusCodes.OK, res.toJSON());
  }
}
