package org.example.handlers.rest;

import static org.example.utils.APIGatewayResponseBuilder.makeHttpResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import java.util.Map;
import java.util.Optional;
import org.example.constants.StatusCodes;
import org.example.entities.Stats;
import org.example.exceptions.InternalServerError;
import org.example.services.StatsService;

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

    Map<String, String> queryParams = event.getQueryStringParameters();

    if (!service.doesUserExist(userId)) {
      return makeHttpResponse(StatusCodes.BAD_REQUEST, "Missing User");
    }

    Stats userStats;
    try {
      userStats = service.getStatsByUserID(userId);
    } catch (InternalServerError e) {
      return e.makeHttpResponse();
    }

    if (queryParams == null) {
      // return with ALL gamemodes
      return makeHttpResponse(StatusCodes.OK, userStats.toJSON());
    }

    String gameMode = queryParams.get("gamemode");

    // gamemode not part of query: bad request
    if (gameMode == null) {
      return makeHttpResponse(
          StatusCodes.BAD_REQUEST, "Query defined, but query parameter \"gamemode\" was missing");
    }

    // missing value for gamemode: bad request
    if (gameMode.isEmpty()) {
      return makeHttpResponse(
          StatusCodes.BAD_REQUEST, "Query parameter \"gamemode\" was missing a value");
    }

    Optional<String> optionalJson = userStats.toJSON(gameMode);

    // invalid value for gamemode: bad request
    if (optionalJson.isEmpty()) {
      return makeHttpResponse(
          StatusCodes.BAD_REQUEST,
          "Query parameter \"gamemode\" had an invalid value: " + gameMode);
    }

    // return with QUERIED gamemode
    return makeHttpResponse(StatusCodes.OK, optionalJson.get());
  }
}
