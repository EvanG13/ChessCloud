package org.example.handlers.rest.stats;

import static org.example.utils.APIGatewayResponseBuilder.makeHttpResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.amazonaws.services.lambda.runtime.logging.LogLevel;
import java.util.Map;
import org.example.constants.StatusCodes;
import org.example.entities.stats.Stats;
import org.example.exceptions.NotFound;

public class StatsHandler
    implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
  private final StatsHandlerService service;

  public StatsHandler() {
    service = new StatsHandlerService();
  }

  public StatsHandler(StatsHandlerService service) {
    this.service = service;
  }

  @Override
  public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
    LambdaLogger logger = context.getLogger();
    Map<String, String> pathParams = event.getPathParameters();

    String username = pathParams.get("username");
    if (username == null) {
      return makeHttpResponse(StatusCodes.BAD_REQUEST, "Missing username");
    }

    Map<String, String> queryParams = event.getQueryStringParameters();

    Stats userStats;
    if (queryParams == null) {
      try {
        userStats = service.getStatsByUsername(username);
      } catch (NotFound e) {
        logger.log(e.getMessage(), LogLevel.FATAL);
        return e.makeHttpResponse();
      }

      return makeHttpResponse(StatusCodes.OK, userStats.toJSON());
    }

    String timeCategory = queryParams.get("timeCategory");
    if (timeCategory == null) {
      logger.log("Query defined, but query parameter \"timeCategory\" was missing", LogLevel.FATAL);
      return makeHttpResponse(
          StatusCodes.BAD_REQUEST,
          "Query defined, but query parameter \"timeCategory\" was missing");
    }

    if (timeCategory.isEmpty()) {
      logger.log("Query parameter \"timeCategory\" was missing a value", LogLevel.FATAL);
      return makeHttpResponse(
          StatusCodes.BAD_REQUEST, "Query parameter \"timeCategory\" was missing a value");
    }

    if (!service.doesCategoryExist(timeCategory)) {
      logger.log("Query parameter \"timeCategory\" had an invalid value: ", LogLevel.FATAL);
      return makeHttpResponse(
          StatusCodes.BAD_REQUEST,
          "Query parameter \"timeCategory\" had an invalid value: " + timeCategory);
    }

    try {
      userStats = service.getStatsByUsernameAndCategory(username, timeCategory);
    } catch (NotFound e) {
      return e.makeHttpResponse();
    }

    return makeHttpResponse(StatusCodes.OK, userStats.toJSON(timeCategory));
  }
}
