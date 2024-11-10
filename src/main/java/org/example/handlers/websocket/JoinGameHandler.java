package org.example.handlers.websocket;

import static org.example.utils.APIGatewayResponseBuilder.makeWebsocketResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
import java.util.Optional;
import org.example.constants.StatusCodes;
import org.example.entities.game.Game;
import org.example.entities.player.Player;
import org.example.entities.stats.Stats;
import org.example.entities.user.User;
import org.example.enums.WebsocketResponseAction;
import org.example.enums.GameMode;
import org.example.exceptions.NotFound;
import org.example.models.requests.JoinGameRequest;
import org.example.models.responses.websocket.GameCreatedMessageData;
import org.example.models.responses.websocket.GameStartedMessageData;
import org.example.models.responses.websocket.SocketResponseBody;
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
      GameStartedMessageData data =
          GameStartedMessageData.builder()
              .isSuccess(false)
              .message("No user matches userId")
              .build();
      SocketResponseBody<GameStartedMessageData> responseBody =
          new SocketResponseBody<>(WebsocketResponseAction.GAME_CREATED, data);
      emitter.sendMessage(connectionId, responseBody.toJSON());

      return makeWebsocketResponse(StatusCodes.UNAUTHORIZED, "No user matches userId");
    }

    Optional<Stats> optionalStats = service.getUserStats(userId);
    if (optionalStats.isEmpty()) {
      GameStartedMessageData data =
          GameStartedMessageData.builder()
              .isSuccess(false)
              .message("User doesn't have entry in Stats collection")
              .build();
      SocketResponseBody<GameStartedMessageData> responseBody =
          new SocketResponseBody<>(WebsocketResponseAction.GAME_CREATED, data);
      emitter.sendMessage(connectionId, responseBody.toJSON());

      return makeWebsocketResponse(
          StatusCodes.INTERNAL_SERVER_ERROR, "User doesn't have entry in Stats collection");
    }

    if (service.isInGame(userId)) {
      // TODO : consider just returning the game they are already in
      GameStartedMessageData data =
          GameStartedMessageData.builder()
              .isSuccess(false)
              .message("You are already in 1 game.")
              .build();
      SocketResponseBody<GameStartedMessageData> responseBody =
          new SocketResponseBody<>(WebsocketResponseAction.GAME_CREATED, data);
      emitter.sendMessage(connectionId, responseBody.toJSON());

      return makeWebsocketResponse(StatusCodes.FORBIDDEN, "You are already in 1 game.");
    }

    User user = optionalUser.get();
    Stats stats = optionalStats.get();
    GameMode gameMode = joinRequestData.timeControl().getGameMode();

    Player newPlayer =
        Player.builder()
            .playerId(userId)
            .connectionId(connectionId)
            .username(user.getUsername())
            .rating(stats.getRating(gameMode))
            .build();

    Game game;
    try {
      game = service.getPendingGame(joinRequestData.timeControl(), stats.getRating(gameMode));
    } catch (NotFound e) {
      game = null;
    }

    String body;
    int statusCode;
    if (game == null) {
      // No pending game for the requested time control
      // Create new game with requested time control

      Game newGame = new Game(joinRequestData.timeControl(), newPlayer);
      service.createGame(newGame);

      statusCode = StatusCodes.CREATED;
      body = newGame.toResponseJson();
      GameCreatedMessageData messageData = new GameCreatedMessageData();
      SocketResponseBody<GameCreatedMessageData> responseBody =
          new SocketResponseBody<>(WebsocketResponseAction.GAME_CREATED, messageData);
      emitter.sendMessage(connectionId, responseBody.toJSON());
    } else {
      // Pending game exists for the requested time control
      // Join pending game
      GameStartedMessageData data;
      SocketResponseBody<GameStartedMessageData> responseBody;
      try {
        game.setup(newPlayer);
      } catch (Exception e) {
        data =
            GameStartedMessageData.builder()
                .isSuccess(false)
                .message("Error in setting up game.")
                .build();
        responseBody = new SocketResponseBody<>(WebsocketResponseAction.GAME_START, data);
        emitter.sendMessages(
            game.getPlayers().get(0).getConnectionId(),
            game.getPlayers().get(1).getConnectionId(),
            responseBody.toJSON());
        return makeWebsocketResponse(
            StatusCodes.INTERNAL_SERVER_ERROR, "Error in setting up game.");
      }

      service.updateGame(game);

      // Notify both players that the game is starting
      data = new GameStartedMessageData(game);
      responseBody = new SocketResponseBody<>(WebsocketResponseAction.GAME_START, data);
      String resJson = responseBody.toJSON();
      emitter.sendMessages(
          game.getPlayers().get(0).getConnectionId(),
          game.getPlayers().get(1).getConnectionId(),
          resJson);

      statusCode = StatusCodes.OK;
      body = resJson;
    }

    return makeWebsocketResponse(statusCode, body);
  }
}
