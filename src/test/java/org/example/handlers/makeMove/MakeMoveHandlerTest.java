package org.example.handlers.makeMove;

import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import org.example.constants.StatusCodes;
import org.example.entities.Player;
import org.example.entities.game.Game;
import org.example.entities.game.GameService;
import org.example.entities.stats.Stats;
import org.example.entities.user.User;
import org.example.enums.Action;
import org.example.enums.TimeControl;
import org.example.exceptions.NotFound;
import org.example.handlers.websocket.JoinGameHandler;
import org.example.handlers.websocket.MakeMoveHandler;
import org.example.models.requests.JoinGameRequest;
import org.example.models.requests.MakeMoveRequest;
import org.example.models.responses.websocket.GameStartedMessageData;
import org.example.models.responses.websocket.MakeMoveMessageData;
import org.example.models.responses.websocket.SocketResponseBody;
import org.example.services.JoinGameService;
import org.example.services.MakeMoveService;
import org.example.utils.MockContext;
import org.example.utils.MongoDBUtility;
import org.example.utils.socketMessenger.SocketSystemLogger;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MakeMoveHandlerTest {
  public static SocketSystemLogger socketLogger;

  public static GameService gameUtility;
  public static MongoDBUtility<User> userUtility;
  public static MongoDBUtility<Stats> statsUtility;

  public static JoinGameHandler joinGameHandler;
  public static MakeMoveService makeMoveService;
  public static JoinGameService joinGameService;

  public static String userId;
  public static String userId2;
  public static String wrongUserId;

  public static String gameId;
  public static TimeControl timeControl;
  public static String connectId;
  public static String connectId2;

  public static String firstMove;
  public static String secondMove;
  public static String thirdMove;
  public static String invalidMove;
  public static String secondInvalidMove;

  public static String username;
  public static String username2;
  public static String email;
  public static String email2;
  public static String password;
  public static String password2;

  public static Gson gson;

  @BeforeAll
  public static void setUp() {
    socketLogger = new SocketSystemLogger();

    gameUtility = new GameService();
    userUtility = new MongoDBUtility<>("users", User.class);
    statsUtility = new MongoDBUtility<>("stats", Stats.class);

    makeMoveService = new MakeMoveService(gameUtility);
    joinGameService = new JoinGameService(gameUtility, userUtility, statsUtility);

    joinGameHandler = new JoinGameHandler(joinGameService, socketLogger);

    firstMove = "e2e4";
    secondMove = "d7d5"; // scandinavian game
    thirdMove = "e4d5"; // pawn takes pawn
    invalidMove = "e2e7";
    secondInvalidMove = "e9";

    userId = "test-Id";
    userId2 = "test-Id2";
    wrongUserId = "this-user-is-not-in-a-game";

    timeControl = TimeControl.BLITZ_5;
    connectId = "fake-connection-id";
    connectId2 = "fake-connection-id2";

    username = "test-username";
    username2 = "test-username2";
    email = "test@gmail.com";
    email2 = "test2@gmail.com";
    password = "1223";
    password2 = "123";

    User testUser =
        User.builder().id(userId).email(email).password(password).username(username).build();
    User testUser2 =
        User.builder().id(userId2).email(email2).password(password2).username(username2).build();
    userUtility.post(testUser);
    userUtility.post(testUser2);

    Stats testUserStats = new Stats(testUser.getId());
    Stats testUserStats2 = new Stats(testUser2.getId());
    statsUtility.post(testUserStats);
    statsUtility.post(testUserStats2);

    gson = new Gson();
  }

  @AfterAll
  public static void tearDown() {
    gameUtility.deleteGame(gameId);

    userUtility.delete(userId);
    userUtility.delete(userId2);

    statsUtility.delete(userId);
    statsUtility.delete(userId2);
  }

  @DisplayName("GAME CREATED ✅")
  @Test
  @Order(1)
  public void returnGameCreated() {

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();

    Context context = new MockContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(connectId);
    requestContext.setRouteKey("joinGame");

    event.setRequestContext(requestContext);
    JoinGameRequest request = new JoinGameRequest("joinGame", userId, timeControl);
    event.setBody(gson.toJson(request));

    APIGatewayV2WebSocketResponse response = joinGameHandler.handleRequest(event, context);
    assertEquals(StatusCodes.CREATED, response.getStatusCode());
  }

  @Test
  @DisplayName("GAME STARTED")
  @Order(2)
  public void returnGameStarted() throws NotFound {
    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(connectId2);
    requestContext.setRouteKey("joinGame");

    event.setRequestContext(requestContext);
    JoinGameRequest request = new JoinGameRequest("joinGame", userId2, timeControl);
    event.setBody(gson.toJson(request));

    APIGatewayV2WebSocketResponse response =
        joinGameHandler.handleRequest(event, new MockContext());
    assertEquals(StatusCodes.OK, response.getStatusCode());

    Type responseType = new TypeToken<SocketResponseBody<GameStartedMessageData>>() {}.getType();
    SocketResponseBody<GameStartedMessageData> body =
        gson.fromJson(response.getBody(), responseType);

    GameStartedMessageData data = body.getData();

    gameId = data.getGameId();

    Game game = gameUtility.get(gameId);
    List<Player> playerList = game.getPlayers();
    assertEquals(2, playerList.size());

    Player player1 = playerList.get(0);
    Player player2 = playerList.get(1);
    assertNotSame(player1.getIsWhite(), player2.getIsWhite());

    if (player2.getIsWhite()) {
      connectId = player2.getConnectionId();
      connectId2 = player1.getConnectionId();
      userId = player2.getPlayerId();
      userId2 = player1.getPlayerId();
    }
  }

  @Test
  @DisplayName("User is not a Player")
  @Order(3)
  public void returnUnauthorized() {
    MakeMoveHandler makeMoveHandler = new MakeMoveHandler(makeMoveService, socketLogger);

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();

    Context context = new MockContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(connectId);
    requestContext.setRouteKey("makeMove");

    event.setRequestContext(requestContext);
    MakeMoveRequest request =
        MakeMoveRequest.builder().gameId(gameId).playerId(wrongUserId).move(firstMove).build();
    event.setBody(gson.toJson(request));

    APIGatewayV2WebSocketResponse response = makeMoveHandler.handleRequest(event, context);
    assertEquals(StatusCodes.UNAUTHORIZED, response.getStatusCode());
    assertEquals("User is not in this Game", response.getBody());
  }

  @Test
  @DisplayName("M1: White - invalid move")
  @Order(4)
  public void returnBadRequest() {
    MakeMoveHandler makeMoveHandler = new MakeMoveHandler(makeMoveService, socketLogger);

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();

    Context context = new MockContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(connectId);
    requestContext.setRouteKey("makeMove");

    event.setRequestContext(requestContext);
    MakeMoveRequest request =
        MakeMoveRequest.builder().gameId(gameId).playerId(userId).move(invalidMove).build();
    event.setBody(gson.toJson(request));

    APIGatewayV2WebSocketResponse response = makeMoveHandler.handleRequest(event, context);
    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
    assertEquals("Illegal Move: " + invalidMove, response.getBody());
  }

  @Test
  @DisplayName("M1: White - successful move")
  @Order(5)
  public void returnOk() {
    MakeMoveHandler makeMoveHandler = new MakeMoveHandler(makeMoveService, socketLogger);

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();

    Context context = new MockContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(connectId);
    requestContext.setRouteKey("makeMove");

    event.setRequestContext(requestContext);
    MakeMoveRequest request =
        MakeMoveRequest.builder().gameId(gameId).playerId(userId).move(firstMove).build();
    event.setBody(gson.toJson(request));

    APIGatewayV2WebSocketResponse response = makeMoveHandler.handleRequest(event, context);
    assertEquals(StatusCodes.OK, response.getStatusCode());

    MakeMoveMessageData data =
        new MakeMoveMessageData(
            "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1",
            new ArrayList<>(List.of("e2e4")),
            false);
    SocketResponseBody<MakeMoveMessageData> expectedResponse =
        new SocketResponseBody<>(Action.MOVE_MADE, data);
    assertEquals(expectedResponse.toJSON(), response.getBody());
  }

  @Test
  @DisplayName("M2: Black - invalid move")
  @Order(6)
  public void returnSecondBadRequest() {
    MakeMoveHandler makeMoveHandler = new MakeMoveHandler(makeMoveService, socketLogger);
    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();

    Context context = new MockContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(connectId2);
    requestContext.setRouteKey("makeMove");

    event.setRequestContext(requestContext);

    MakeMoveRequest request =
        MakeMoveRequest.builder().gameId(gameId).playerId(userId2).move(secondInvalidMove).build();
    event.setBody(gson.toJson(request));

    APIGatewayV2WebSocketResponse response = makeMoveHandler.handleRequest(event, context);
    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
    assertEquals("Illegal Move: " + secondInvalidMove, response.getBody());
  }

  @Test
  @DisplayName("M2: White - tried to move out of turn")
  @Order(7)
  public void returnMovedOutOfTurn() {
    MakeMoveHandler makeMoveHandler = new MakeMoveHandler(makeMoveService, socketLogger);

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();

    Context context = new MockContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(connectId);
    requestContext.setRouteKey("makeMove");

    event.setRequestContext(requestContext);

    MakeMoveRequest request =
        MakeMoveRequest.builder().gameId(gameId).playerId(userId).move(thirdMove).build();
    event.setBody(gson.toJson(request));

    APIGatewayV2WebSocketResponse response = makeMoveHandler.handleRequest(event, context);
    assertEquals(StatusCodes.FORBIDDEN, response.getStatusCode());
    assertEquals("It is not your turn.", response.getBody());
  }

  @Test
  @DisplayName("M2: Black - successful move")
  @Order(8)
  public void returnSuccessfulSecondMove() {
    MakeMoveHandler makeMoveHandler = new MakeMoveHandler(makeMoveService, socketLogger);

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();

    Context context = new MockContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(connectId2);
    requestContext.setRouteKey("makeMove");

    event.setRequestContext(requestContext);
    MakeMoveRequest request =
        MakeMoveRequest.builder().gameId(gameId).playerId(userId2).move(secondMove).build();
    event.setBody(gson.toJson(request));

    APIGatewayV2WebSocketResponse response = makeMoveHandler.handleRequest(event, context);
    assertEquals(StatusCodes.OK, response.getStatusCode());

    MakeMoveMessageData data =
        new MakeMoveMessageData(
            "rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2",
            new ArrayList<>(List.of("e2e4", "d7d5")),
            true);
    SocketResponseBody<MakeMoveMessageData> expectedResponse =
        new SocketResponseBody<>(Action.MOVE_MADE, data);
    assertEquals(expectedResponse.toJSON(), response.getBody());
  }

  @Test
  @DisplayName("M3: White - successful move")
  @Order(9)
  public void returnSuccessfulThirdMove() {
    MakeMoveHandler makeMoveHandler = new MakeMoveHandler(makeMoveService, socketLogger);

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();

    Context context = new MockContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(connectId);
    requestContext.setRouteKey("makeMove");

    event.setRequestContext(requestContext);
    MakeMoveRequest request =
        MakeMoveRequest.builder().gameId(gameId).playerId(userId).move(thirdMove).build();
    event.setBody(gson.toJson(request));

    APIGatewayV2WebSocketResponse response = makeMoveHandler.handleRequest(event, context);
    assertEquals(StatusCodes.OK, response.getStatusCode());

    MakeMoveMessageData data =
        new MakeMoveMessageData(
            "rnbqkbnr/ppp1pppp/8/3P4/8/8/PPPP1PPP/RNBQKBNR b KQkq - 0 2",
            new ArrayList<>(List.of("e2e4", "d7d5", "e4d5")),
            false);
    SocketResponseBody<MakeMoveMessageData> expectedResponse =
        new SocketResponseBody<>(Action.MOVE_MADE, data);
    assertEquals(expectedResponse.toJSON(), response.getBody());
  }
}
