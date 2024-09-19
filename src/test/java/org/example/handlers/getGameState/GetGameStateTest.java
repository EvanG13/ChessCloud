package org.example.handlers.getGameState;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.github.bhlangonijr.chesslib.Board;
import com.google.gson.Gson;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.example.constants.ChessConstants;
import org.example.constants.StatusCodes;
import org.example.entities.Game;
import org.example.entities.Player;
import org.example.entities.Stats;
import org.example.entities.User;
import org.example.enums.GameStatus;
import org.example.enums.TimeControl;
import org.example.handlers.rest.GetGameStateHandler;
import org.example.handlers.websocket.JoinGameHandler;
import org.example.handlers.websocket.MakeMoveHandler;
import org.example.services.GameStateService;
import org.example.services.JoinGameService;
import org.example.services.MakeMoveService;
import org.example.utils.MockContext;
import org.example.utils.MongoDBUtility;
import org.example.utils.socketMessenger.SocketSystemLogger;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class GetGameStateTest {

  public static SocketSystemLogger socketLogger;

  public static MongoDBUtility<Game> gameUtility;
  public static MongoDBUtility<User> userUtility;
  public static MongoDBUtility<Stats> statsUtility;

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

  @BeforeAll
  public static void setUp() {
    socketLogger = new SocketSystemLogger();

    gameUtility = new MongoDBUtility<>("games", Game.class);
    userUtility = new MongoDBUtility<>("users", User.class);
    statsUtility = new MongoDBUtility<>("stats", Stats.class);

    makeMoveService = new MakeMoveService(gameUtility, new Board());
    joinGameService = new JoinGameService(gameUtility, userUtility, statsUtility);

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
  }

  @AfterAll
  public static void tearDown() {
    gameUtility.delete(gameId);

    userUtility.delete(userId);
    userUtility.delete(userId2);

    statsUtility.delete(userId);
    statsUtility.delete(userId2);
  }

  @DisplayName("Game Created ✅")
  @Test
  @Order(1)
  public void returnGameCreated() {
    JoinGameHandler joinGameHandler = new JoinGameHandler(joinGameService, socketLogger);

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();

    Context context = new MockContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(connectId);
    requestContext.setRouteKey("joinGame");

    event.setRequestContext(requestContext);
    event.setBody(
        "{'action' : 'joinGame', 'timeControl': '"
            + timeControl
            + "', 'userId': '"
            + userId
            + "'}");

    APIGatewayV2WebSocketResponse response = joinGameHandler.handleRequest(event, context);
    assertEquals(StatusCodes.CREATED, response.getStatusCode());

    String gameJson = response.getBody();
    gameId = (new Gson()).fromJson(gameJson, Game.class).getId();

    Player newPlayer =
        Player.builder()
            .playerId(userId)
            .connectionId(connectId)
            .username(username)
            .rating(ChessConstants.BASE_RATING) // new player default rating
            .build();

    Game expected = new Game(timeControl, newPlayer);
    expected.setId(
        gameId); // since calling the constructor will autoincrement the id from the last game

    Optional<Game> optionalGame = gameUtility.get(gameId);

    assertFalse(optionalGame.isEmpty());
    assertEquals(expected, optionalGame.get());
  }

  @Test
  @DisplayName("Game Started")
  @Order(2)
  public void returnGameStarted() {
    JoinGameHandler joinGameHandler = new JoinGameHandler(joinGameService, socketLogger);

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();

    Context context = new MockContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(connectId2);
    requestContext.setRouteKey("joinGame");

    event.setRequestContext(requestContext);
    event.setBody(
        "{'action' : 'joinGame', 'timeControl': '"
            + timeControl
            + "', 'userId': '"
            + userId2
            + "'}");

    APIGatewayV2WebSocketResponse response = joinGameHandler.handleRequest(event, context);
    assertEquals(StatusCodes.OK, response.getStatusCode());

    Optional<Game> optionalGame = gameUtility.get(gameId);
    assertFalse(optionalGame.isEmpty());

    Game game = optionalGame.get();
    assertEquals(GameStatus.ONGOING, game.getGameStatus());
    assertTrue(game.getIsWhitesTurn());

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
  @DisplayName("M1: White - successful move")
  @Order(3)
  public void returnOk() {
    MakeMoveHandler makeMoveHandler = new MakeMoveHandler(makeMoveService, socketLogger);

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();

    Context context = new MockContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(connectId);
    requestContext.setRouteKey("makeMove");

    event.setRequestContext(requestContext);
    event.setBody(
        "{'action' : 'makeMove', 'gameId': '"
            + gameId
            + "', 'playerId': '"
            + userId
            + "', 'move': '"
            + firstMove
            + "'}");

    APIGatewayV2WebSocketResponse response = makeMoveHandler.handleRequest(event, context);
    assertEquals(StatusCodes.OK, response.getStatusCode());
    assertEquals(
        "{\"fen\":\"rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1\",\"moveList\":[\"e2e4\"]}",
        response.getBody());
  }

  @Test
  @DisplayName("M2: Black - successful move")
  @Order(4)
  public void returnSuccessfulSecondMove() {
    MakeMoveHandler makeMoveHandler = new MakeMoveHandler(makeMoveService, socketLogger);

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();

    Context context = new MockContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(connectId2);
    requestContext.setRouteKey("makeMove");

    event.setRequestContext(requestContext);
    event.setBody(
        "{'action' : 'makeMove', 'gameId': '"
            + gameId
            + "', 'playerId': '"
            + userId2
            + "', 'move': '"
            + secondMove
            + "'}");

    APIGatewayV2WebSocketResponse response = makeMoveHandler.handleRequest(event, context);
    assertEquals(StatusCodes.OK, response.getStatusCode());
    assertEquals(
        "{\"fen\":\"rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 2\",\"moveList\":[\"e2e4\",\"d7d5\"]}",
        response.getBody());
  }

  @Test
  @DisplayName("M3: White - successful move")
  @Order(5)
  public void returnSuccessfulThirdMove() {
    MakeMoveHandler makeMoveHandler = new MakeMoveHandler(makeMoveService, socketLogger);

    APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();

    Context context = new MockContext();

    APIGatewayV2WebSocketEvent.RequestContext requestContext =
        new APIGatewayV2WebSocketEvent.RequestContext();
    requestContext.setConnectionId(connectId);
    requestContext.setRouteKey("makeMove");

    event.setRequestContext(requestContext);
    event.setBody(
        "{'action' : 'makeMove', 'gameId': '"
            + gameId
            + "', 'playerId': '"
            + userId
            + "', 'move': '"
            + thirdMove
            + "'}");

    APIGatewayV2WebSocketResponse response = makeMoveHandler.handleRequest(event, context);
    assertEquals(StatusCodes.OK, response.getStatusCode());
    assertEquals(
        "{\"fen\":\"rnbqkbnr/ppp1pppp/8/3P4/8/8/PPPP1PPP/RNBQKBNR b KQkq - 0 2\",\"moveList\":[\"e2e4\",\"d7d5\",\"e4d5\"]}",
        response.getBody());
  }

  @Test
  @DisplayName("Get Game State")
  @Order(6)
  public void returnSuccessfulGameState() {
    GetGameStateHandler getGameStateHandler =
        new GetGameStateHandler(new GameStateService(gameUtility));
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
    event.setHeaders(Map.of("userid", userId));
    Context context = new MockContext();

    APIGatewayV2HTTPEvent.RequestContext requestContext =
        new APIGatewayV2HTTPEvent.RequestContext();

    event.setRequestContext(requestContext);
    APIGatewayV2HTTPResponse response = getGameStateHandler.handleRequest(event, context);
    assertEquals(StatusCodes.OK, response.getStatusCode());
    Optional<Game> optionalGame = gameUtility.get(gameId);
    assertTrue(optionalGame.isPresent());
    Game game = optionalGame.get();

    assertEquals(game.toResponseJson(), response.getBody());
  }

  @Test
  @DisplayName("User is not in a game")
  @Order(7)
  public void returnNotFound() {
    GetGameStateHandler getGameStateHandler =
        new GetGameStateHandler(new GameStateService(gameUtility));
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
    event.setHeaders(Map.of("userid", wrongUserId));
    Context context = new MockContext();

    APIGatewayV2HTTPEvent.RequestContext requestContext =
        new APIGatewayV2HTTPEvent.RequestContext();

    event.setRequestContext(requestContext);
    APIGatewayV2HTTPResponse response = getGameStateHandler.handleRequest(event, context);
    assertEquals(StatusCodes.NOT_FOUND, response.getStatusCode());
  }
}
