package org.example.handlers.rest;

import static org.example.utils.APIGatewayResponseBuilder.makeHttpResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import java.util.Map;
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
      LambdaLogger logger = context.getLogger();
      logger.log("User " + userId + " is missing stats", LogLevel.FATAL);
      return e.makeHttpResponse();
    }

    // return with stats for ALL game modes
    if (queryParams == null) {
      return makeHttpResponse(StatusCodes.OK, userStats.toJSON());
    }

    String gameMode = queryParams.get("gamemode");

    // gamemode not part of query
    if (gameMode == null) {
      return makeHttpResponse(
          StatusCodes.BAD_REQUEST,
          "Query defined, but query parameter \"gamemode\" was missing");
    }

    // no value for gamemode
    if (gameMode.isEmpty()) {
      return makeHttpResponse(
          StatusCodes.BAD_REQUEST,
          "Query parameter \"gamemode\" was missing a value");
    }

    // gamemode doesn't exist or is not supported
    if (!userStats.doesGamemodeHaveStats(gameMode)) {
      return makeHttpResponse(
          StatusCodes.BAD_REQUEST,
          "Query parameter \"gamemode\" had an invalid value: " + gameMode);
    }

    // return with stats for QUERIED game mode
    return makeHttpResponse(StatusCodes.OK, userStats.toJSON(gameMode));
  }
}
