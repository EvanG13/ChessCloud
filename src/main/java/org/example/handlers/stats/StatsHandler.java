package org.example.handlers.stats;

import static org.example.handlers.Responses.makeHttpResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import java.util.Optional;
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

    Optional<User> optionalUser = service.getByID(userId);

    if (optionalUser.isEmpty()) {
      return makeHttpResponse(StatusCodes.BAD_REQUEST, "Missing User");
    }

    User user = optionalUser.get();

    return makeHttpResponse(StatusCodes.OK, user.toStatsJSON());
  }
}
