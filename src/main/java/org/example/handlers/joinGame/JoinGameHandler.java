package org.example.handlers.joinGame;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
import java.util.Optional;
import org.example.entities.Game;
import org.example.entities.Player;
import org.example.entities.User;
import org.example.requestRecords.JoinGameRequest;
import org.example.statusCodes.StatusCodes;
import org.example.utils.SocketEmitter;

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

    System.out.println(requestContext);
    System.out.println(event);

    Gson gson = new Gson();
    JoinGameRequest joinRequestData = gson.fromJson(event.getBody(), JoinGameRequest.class);
    System.out.println(joinRequestData.timeControl() + " " + joinRequestData.userId());
    // TODO: find game using matchmaking logic
    // if no game exists then create a game
    String userId = joinRequestData.userId();
    Optional<User> optionalUser = service.getUser(userId);
    if (optionalUser.isEmpty()) {
      response.setStatusCode(StatusCodes.UNAUTHORIZED);
      System.out.println("user not found");
      return response;
    }
    User user = optionalUser.get();
    System.out.println(user);
    String username = user.getUsername();
    Optional<Game> optionalGame = service.getPendingGame(joinRequestData.timeControl());
    Player newPlayer =
        Player.builder()
            .playerId(userId)
            .connectionId(requestContext.getConnectionId())
            .username(username) // could add rating and things later
            .build();
    if (optionalGame.isEmpty()) {
      // create a game
      System.out.println("game is empty");
      Game newGame = new Game(joinRequestData.timeControl(), newPlayer);
      service.createGame(newGame);
      response.setStatusCode(StatusCodes.CREATED);
    } else {
      System.out.println("game found!");
      // join this user to the game
      Game game = optionalGame.get();
      System.out.println(game);
      game.setup(newPlayer);
      // save the game in the database
      // notify both players that the game is starting
      SocketEmitter.sendMessages(
          game.getPlayers().get(0).getConnectionId(),
          game.getPlayers().get(1).getConnectionId(),
          "game is started");
      response.setStatusCode(StatusCodes.OK);
    }

    return response;
  }
}
