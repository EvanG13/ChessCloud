package org.example.handlers.websocket;

import static org.example.utils.APIGatewayResponseBuilder.makeWebsocketResponse;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
import org.example.constants.StatusCodes;
import org.example.entities.game.Game;
import org.example.enums.WebsocketResponseAction;
import org.example.enums.GameStatus;
import org.example.models.requests.MessageRequest;
import org.example.models.responses.websocket.ChatMessageData;
import org.example.models.responses.websocket.SocketResponseBody;
import org.example.services.GameStateService;
import org.example.utils.socketMessenger.SocketEmitter;

public class MessageHandler
    implements RequestHandler<APIGatewayV2WebSocketEvent, APIGatewayV2WebSocketResponse> {
  SocketEmitter emitter;
  GameStateService gameService;

  public MessageHandler() {
    emitter = new SocketEmitter();
    gameService = new GameStateService();
  }

  public MessageHandler(SocketEmitter emitter, GameStateService service) {
    this.emitter = emitter;
    this.gameService = service;
  }

  @Override
  public APIGatewayV2WebSocketResponse handleRequest(
      APIGatewayV2WebSocketEvent event, Context context) {

    // TODO eventually try fetching all the players in the game room's socket ids and

    APIGatewayV2WebSocketEvent.RequestContext requestContext = event.getRequestContext();

    if (requestContext == null || requestContext.getConnectionId() == null) {
      return makeWebsocketResponse(
          StatusCodes.BAD_REQUEST, "Invalid event: missing requestContext or connectionId");
    }
    String connectionId = requestContext.getConnectionId();
    MessageRequest messageRequest = (new Gson()).fromJson(event.getBody(), MessageRequest.class);

    String userId = messageRequest.userId();
    String chatMessage = messageRequest.chatMessage();
    String username = messageRequest.username();

    Game game;
    ChatMessageData data;
    SocketResponseBody<ChatMessageData> responseBody;
    try {
      game = gameService.getGameFromUserID(userId);
    } catch (Exception e) {
      LambdaLogger logger = context.getLogger();
      logger.log(e.getMessage());
      data = ChatMessageData.builder().isSuccess(false).message("game not found").build();
      responseBody = new SocketResponseBody<>(WebsocketResponseAction.CHAT_MESSAGE, data);
      emitter.sendMessage(connectionId, responseBody.toJSON());
      return makeWebsocketResponse(StatusCodes.NOT_FOUND, "Game not found");
    }
    if (!game.getGameStatus().equals(GameStatus.ONGOING) || game.getPlayers().size() != 2) {
      data =
          ChatMessageData.builder().isSuccess(false).message("Cannot find chat recipient.").build();
      responseBody = new SocketResponseBody<>(WebsocketResponseAction.CHAT_MESSAGE, data);
      emitter.sendMessage(connectionId, responseBody.toJSON());
      return makeWebsocketResponse(StatusCodes.BAD_REQUEST, "Cannot find chat recipient.");
    }
    data = new ChatMessageData(username + ": " + chatMessage);
    responseBody = new SocketResponseBody<>(WebsocketResponseAction.CHAT_MESSAGE, data);
    emitter.sendMessages(
        game.getPlayers().get(0).getConnectionId(),
        game.getPlayers().get(1).getConnectionId(),
        responseBody.toJSON());

    return makeWebsocketResponse(StatusCodes.OK, "OK");
  }
}
