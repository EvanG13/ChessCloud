package org.example.handlers.websocket;

import static org.example.utils.APIGatewayResponseBuilder.makeWebsocketResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
import java.util.Optional;
import org.example.constants.StatusCodes;
import org.example.entities.Game;
import org.example.entities.Player;
import org.example.entities.Stats;
import org.example.entities.User;
import org.example.enums.GameMode;
import org.example.models.requests.JoinGameRequest;
import org.example.services.JoinGameService;
import org.example.utils.socketMessenger.SocketEmitter;
import org.example.utils.socketMessenger.SocketMessenger;

public class JoinGameHandler
    implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {

  private final JoinGameService service;
  private final SocketMessenger emitter;

  public JoinGameHandler() {
    service = new JoinGameService();
    emitter = new SocketEmitter();
  }

  public JoinGameHandler(JoinGameService service, SocketMessenger emitter) {
    this.service = service;
    this.emitter = emitter;
  }

  @Override
  public APIGatewayV2WebSocketResponse handleRequest(
      APIGatewayV2WebSocketEvent event, Context context) {
    APIGatewayV2WebSocketEvent.RequestContext requestContext = event.getRequestContext();

    if (requestContext == null || requestContext.getConnectionId() == null) {
      return makeWebsocketResponse(
          StatusCodes.BAD_REQUEST, "Invalid event: missing requestContext or connectionId");
    }

    String connectionId = requestContext.getConnectionId();

    Gson gson = new Gson();
    JoinGameRequest joinRequestData = gson.fromJson(event.getBody(), JoinGameRequest.class);

    String userId = joinRequestData.userId();
    Optional<User> optionalUser = service.getUser(userId);
    if (optionalUser.isEmpty()) {
      return makeWebsocketResponse(StatusCodes.UNAUTHORIZED, "No user matches userId");
    }

    Optional<Stats> optionalStats = service.getUserStats(userId);
    if (optionalStats.isEmpty()) {
      return makeWebsocketResponse(
          StatusCodes.INTERNAL_SERVER_ERROR, "User doesn't have entry in Stats collection");
    }

    if (service.isInGame(userId)) {
      return makeWebsocketResponse(StatusCodes.FORBIDDEN, "You are already in 1 game.");
    }

    User user = optionalUser.get();
    Stats stats = optionalStats.get();
    GameMode gameMode = joinRequestData.timeControl().getGameMode();

    Optional<Game> optionalGame =
        service.getPendingGame(joinRequestData.timeControl(), stats.getRating(gameMode));

    Player newPlayer =
        Player.builder()
            .playerId(userId)
            .connectionId(connectionId)
            .username(user.getUsername())
            .rating(stats.getRating(gameMode))
            .build();

    String body;
    int statusCode;
    if (optionalGame.isEmpty()) {
      // No pending game for the requested time control
      // Create new game with requested time control

      Game newGame = new Game(joinRequestData.timeControl(), newPlayer);
      service.createGame(newGame);

      statusCode = StatusCodes.CREATED;
      body = newGame.toResponseJson();

      emitter.sendMessage(connectionId, "Created new game. Waiting for someone to join");
    } else {
      // Pending game exists for the requested time control
      // Join pending game

      Game game = optionalGame.get();
      try {
        game.setup(newPlayer);
      } catch (Exception e) {
        return makeWebsocketResponse(StatusCodes.INTERNAL_SERVER_ERROR, "Error in setting up game");
      }

      service.updateGame(game);

      // Notify both players that the game is
      String gameJson = game.toResponseJson();
      emitter.sendMessages(
          game.getPlayers().get(0).getConnectionId(),
          game.getPlayers().get(1).getConnectionId(),
          "game is started: \n" + gameJson);

      statusCode = StatusCodes.OK;
      body = gameJson;
    }

    return makeWebsocketResponse(statusCode, body);
  }
}
