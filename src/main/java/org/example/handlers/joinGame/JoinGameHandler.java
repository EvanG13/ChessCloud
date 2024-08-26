package org.example.handlers.joinGame;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
import java.util.Optional;
import org.example.entities.Game;
import org.example.entities.Player;
import org.example.requestRecords.JoinGameRequest;
import org.example.statusCodes.StatusCodes;

public class JoinGameHandler
    implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {

  private final JoinGameService service;

  public JoinGameHandler() {
    service = new JoinGameService();
  }

  public JoinGameHandler(JoinGameService service) {
    this.service = service;
  }

  @Override
  /*
   * returns a status of 200 if a game was found and we were able to join
   * returns a status of 201 if no game was found to join so we created a game and are awaiting a second player
   * */
  public APIGatewayV2WebSocketResponse handleRequest(
      APIGatewayV2WebSocketEvent event, Context context) {
    APIGatewayV2WebSocketEvent.RequestContext requestContext = event.getRequestContext();
    APIGatewayV2WebSocketResponse response = new APIGatewayV2WebSocketResponse();

    if (requestContext == null || requestContext.getConnectionId() == null) {
      System.err.println("Invalid event: missing requestContext or connectionId");
      response.setStatusCode(StatusCodes.BAD_REQUEST);
      return response;
    }

    Gson gson = new Gson();
    JoinGameRequest joinRequestData = gson.fromJson(event.getBody(), JoinGameRequest.class);
    // TODO: find game using matchmaking logic
    // if no game exists then create a game

    Optional<Game> optionalGame = service.getPendingGame(joinRequestData.timeControl());
    Player newPlayer =
        Player.builder()
            .playerId(joinRequestData.userId())
            .connectionId(requestContext.getConnectionId())
            .playerId(joinRequestData.userId())
            .build();
    if (optionalGame.isEmpty()) {
      // create a game
      Game newGame = new Game(joinRequestData.timeControl(), newPlayer);
      response.setStatusCode(StatusCodes.CREATED);
    } else {
      // join this user to the game
      Game game = optionalGame.get();
      game.setup(newPlayer);
      // notify both players that the game is starting via apigatewaymanagementapiClient
      response.setStatusCode(StatusCodes.OK);
    }

    return response;
  }
}
