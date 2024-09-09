package org.example.handlers.stats;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import java.util.Optional;
import org.example.entities.User;
import org.example.statusCodes.StatusCodes;
import org.example.utils.AuthHeaders;

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
    String userId = event.getHeaders().get("userId").replace("userId ", "").replace("\"", "");
    String sessionToken =
        event.getHeaders().get("Authorization").replace("Bearer ", "").replace("\"", "");

    Optional<User> optionalUser = service.getByID(userId);

    if (optionalUser.isEmpty()) {
      return APIGatewayV2HTTPResponse.builder()
          .withHeaders(AuthHeaders.getCorsHeaders())
          .withBody("Missing User")
          .withStatusCode(StatusCodes.BAD_REQUEST)
          .build();
    }

    User user = optionalUser.get();
    String statsJSON = user.toStatsJSON();

    return APIGatewayV2HTTPResponse.builder()
        .withHeaders(AuthHeaders.getCorsHeaders())
        .withBody(statsJSON)
        .withStatusCode(StatusCodes.OK)
        .build();
  }
}
