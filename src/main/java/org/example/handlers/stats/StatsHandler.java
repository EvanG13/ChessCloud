package org.example.handlers.stats;

import static org.example.handlers.Responses.makeHttpResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import java.util.Map;
import java.util.Optional;
import org.example.entities.Stats;
import org.example.entities.User;
import org.example.statusCodes.StatusCodes;

public class StatsHandler
    implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
  private final StatsService service;

  public StatsHandler() {
    service = new StatsService();
  }

  public StatsHandler(StatsService service) {
    this.service = service;
  }

  @Override
  public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
    String userId = event.getHeaders().get("userid");

    // get query parameters
    Map<String, String> queryParams = event.getQueryStringParameters();

    // check user exists
    Optional<User> optionalUser = service.getUserByID(userId);
    if (optionalUser.isEmpty()) {
      return makeHttpResponse(StatusCodes.BAD_REQUEST, "Missing User");
    }

    // check user has stats
    Optional<Stats> optionalUserStats = service.getStatsByUserID(userId);
    if (optionalUserStats.isEmpty()) {
      return makeHttpResponse(StatusCodes.INTERNAL_SERVER_ERROR, "Missing User's Stats");
    }

    Stats userStats = optionalUserStats.get();

    // if query specified
    if (queryParams != null) {
      // try to get possible query parameters
      String queryGamemode = queryParams.get("gamemode");

      // gamemode not part of query: bad request
      if (queryGamemode == null) {
        return makeHttpResponse(
            StatusCodes.BAD_REQUEST,
            "Query defined, but query parameter \"gamemode\" was missing"
        );
      }

      // missing value for gamemode: bad request
      if (queryGamemode.isEmpty()) {
        return makeHttpResponse(
            StatusCodes.BAD_REQUEST,
            "Query parameter \"gamemode\" was missing a value"
        );
      }

      Optional<String> optionalJson = userStats.toJSON(queryGamemode);

      // invalid value for gamemode: bad request
      if (optionalJson.isEmpty()) {
        return makeHttpResponse(
            StatusCodes.BAD_REQUEST,
            "Query parameter \"gamemode\" had an invalid value: " + queryGamemode
        );
      }

      // return with QUERIED gamemode
      return makeHttpResponse(StatusCodes.OK, optionalJson.get());
    }

    // return with ALL gamemodes
    return makeHttpResponse(StatusCodes.OK, userStats.toJSON());
  }
}
