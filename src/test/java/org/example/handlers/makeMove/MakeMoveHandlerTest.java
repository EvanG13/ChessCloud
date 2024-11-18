package org.example.handlers.makeMove;

import static org.example.utils.WebsocketTestUtils.*;
import static org.junit.jupiter.api.Assertions.*;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.example.constants.StatusCodes;
import org.example.entities.game.ArchivedGameDbService;
import org.example.entities.game.Game;
import org.example.entities.game.GameDbService;
import org.example.entities.move.Move;
import org.example.entities.player.Player;
import org.example.entities.stats.Stats;
import org.example.entities.user.User;
import org.example.enums.TimeControl;
import org.example.enums.WebsocketResponseAction;
import org.example.exceptions.NotFound;
import org.example.handlers.websocket.joinGame.JoinGameHandler;
import org.example.handlers.websocket.joinGame.JoinGameService;
import org.example.handlers.websocket.makeMove.MakeMoveHandler;
import org.example.handlers.websocket.makeMove.MakeMoveService;
import org.example.models.requests.JoinGameRequest;
import org.example.models.requests.MakeMoveRequest;
import org.example.models.responses.websocket.GameStartedMessageData;
import org.example.models.responses.websocket.MakeMoveMessageData;
import org.example.models.responses.websocket.SocketResponseBody;
import org.example.utils.MongoDBUtility;
import org.example.utils.socketMessenger.SocketSystemLogger;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class MakeMoveHandlerTest {
  public static SocketSystemLogger socketLogger;

  public static GameDbService gameUtility;
  public static MongoDBUtility<User> userUtility;
  public static MongoDBUtility<Stats> statsUtility;

  public static JoinGameHandler joinGameHandler;
  public static MakeMoveService makeMoveService;
  public static JoinGameService joinGameService;

  public static String userId;
  public static String userId2;
  public static String wrongUserId;

  public static String gameId;
  public static final String gameId2 = "secondGameId";
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

  public static String checkmateInOneFEN;
  public static String checkmateMove;

  public static String stalemateInOneFEN;
  public static String stalemateMove;

  @BeforeAll
  public static void setUp() {
    socketLogger = new SocketSystemLogger();

    gameUtility = new GameDbService();
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

    // setup to check checkmate
    checkmateInOneFEN = "rnbqkbnr/p2p1ppp/1p6/2p1p3/2B1P3/5Q2/PPPP1PPP/RNB1K1NR w KQkq - 0 1";
    checkmateMove = "f3f7";

    // setup to draw
    stalemateInOneFEN = "1k6/3Q2R1/8/8/4P3/8/PPP3P1/RNB1K1N1 w KQkq - 0 1";
    stalemateMove = "d7c6";

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
    gameUtility.deleteGame(gameId2);

    userUtility.delete(userId);
    userUtility.delete(userId2);

    statsUtility.delete(userId);
    statsUtility.delete(userId2);

    ArchivedGameDbService archivedService = ArchivedGameDbService.builder().build();
    archivedService.deleteArchivedGame(gameId);
    archivedService.deleteArchivedGame(gameId2);
  }

  @DisplayName("GAME CREATED ✅")
  @Test
  @Order(1)
  public void returnGameCreated() {
    APIGatewayV2WebSocketResponse response = getResponse(
        joinGameHandler,
        gson.toJson(new JoinGameRequest(userId, timeControl)),
        makeRequestContext("joinGame", connectId)
    );

    assertEquals(StatusCodes.CREATED, response.getStatusCode());
  }

  @Test
  @DisplayName("GAME STARTED")
  @Order(2)
  public void returnGameStarted() throws NotFound {
    APIGatewayV2WebSocketResponse response = getResponse(
        joinGameHandler,
        gson.toJson(new JoinGameRequest(userId2, timeControl)),
        makeRequestContext("joinGame", connectId2)
    );

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
    MakeMoveRequest request =
        MakeMoveRequest.builder().gameId(gameId).playerId(wrongUserId).move(firstMove).build();

    APIGatewayV2WebSocketResponse response = getResponse(
        new MakeMoveHandler(makeMoveService, socketLogger),
        gson.toJson(request),
        makeRequestContext("makeMove", connectId)
    );

    assertResponse(response, StatusCodes.UNAUTHORIZED, "User is not in this Game");
  }

  @Test
  @DisplayName("M1: White - invalid move")
  @Order(4)
  public void returnBadRequest() {
    MakeMoveRequest request =
        MakeMoveRequest.builder().gameId(gameId).playerId(userId).move(invalidMove).build();

    APIGatewayV2WebSocketResponse response = getResponse(
        new MakeMoveHandler(makeMoveService, socketLogger),
        gson.toJson(request),
        makeRequestContext("makeMove", connectId)
    );

    assertResponse(response, StatusCodes.BAD_REQUEST, "Illegal Move: " + invalidMove);
  }

  @Test
  @DisplayName("M1: White - successful move")
  @Order(5)
  public void returnOk() {
    MakeMoveRequest request =
        MakeMoveRequest.builder().gameId(gameId).playerId(userId).move(firstMove).build();

    APIGatewayV2WebSocketResponse response = getResponse(
        new MakeMoveHandler(makeMoveService, socketLogger),
        gson.toJson(request),
        makeRequestContext("makeMove", connectId)
    );

    assertEquals(StatusCodes.OK, response.getStatusCode());

    Move moveOne =
        Move.builder()
            .moveAsUCI("e2e4")
            .moveAsSan("e4")
            .duration(1)
            .fen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1")
            .build();
    MakeMoveMessageData data =
        new MakeMoveMessageData(
            "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1",
            Arrays.asList(moveOne),
            false,
            299,
            300);
    SocketResponseBody<MakeMoveMessageData> expectedResponse =
        new SocketResponseBody<>(WebsocketResponseAction.MOVE_MADE, data);
    assertEquals(expectedResponse.toJSON(), response.getBody());
  }

  @Test
  @DisplayName("M2: Black - invalid move")
  @Order(6)
  public void returnSecondBadRequest() {
    MakeMoveRequest request =
        MakeMoveRequest.builder().gameId(gameId).playerId(userId2).move(secondInvalidMove).build();

    APIGatewayV2WebSocketResponse response = getResponse(
        new MakeMoveHandler(makeMoveService, socketLogger),
        gson.toJson(request),
        makeRequestContext("makeMove", connectId2)
    );

    assertResponse(response, StatusCodes.BAD_REQUEST, "Illegal Move: " + secondInvalidMove);
  }

  @Test
  @DisplayName("M2: White - tried to move out of turn")
  @Order(7)
  public void returnMovedOutOfTurn() {
    MakeMoveRequest request =
        MakeMoveRequest.builder().gameId(gameId).playerId(userId).move(thirdMove).build();

    APIGatewayV2WebSocketResponse response = getResponse(
        new MakeMoveHandler(makeMoveService, socketLogger),
        gson.toJson(request),
        makeRequestContext("makeMove", connectId)
    );

    assertResponse(response, StatusCodes.FORBIDDEN, "It is not your turn.");
  }

  @Test
  @DisplayName("M2: Black - successful move")
  @Order(8)
  public void returnSuccessfulSecondMove() {
    MakeMoveRequest request =
        MakeMoveRequest.builder().gameId(gameId).playerId(userId2).move(secondMove).build();

    APIGatewayV2WebSocketResponse response = getResponse(
        new MakeMoveHandler(makeMoveService, socketLogger),
        gson.toJson(request),
        makeRequestContext("makeMove", connectId2)
    );

    assertEquals(StatusCodes.OK, response.getStatusCode());

    Move moveOne =
        Move.builder()
            .moveAsUCI("e2e4")
            .moveAsSan("e4")
            .duration(1)
            .fen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1")
            .build();

    Move moveTwo =
        Move.builder()
            .moveAsUCI("d7d5")
            .moveAsSan("d5")
            .duration(1)
            .fen("rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2")
            .build();
    MakeMoveMessageData data =
        new MakeMoveMessageData(
            "rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2",
            Arrays.asList(moveOne, moveTwo),
            true,
            299,
            299);
    SocketResponseBody<MakeMoveMessageData> expectedResponse =
        new SocketResponseBody<>(WebsocketResponseAction.MOVE_MADE, data);
    assertEquals(expectedResponse.toJSON(), response.getBody());
  }

  @Test
  @DisplayName("M3: White - successful move")
  @Order(9)
  public void returnSuccessfulThirdMove() throws NotFound {
    MakeMoveRequest request =
        MakeMoveRequest.builder().gameId(gameId).playerId(userId).move(thirdMove).build();

    APIGatewayV2WebSocketResponse response = getResponse(
        new MakeMoveHandler(makeMoveService, socketLogger),
        gson.toJson(request),
        makeRequestContext("makeMove", connectId)
    );

    assertEquals(StatusCodes.OK, response.getStatusCode());

    Move moveOne =
        Move.builder()
            .moveAsUCI("e2e4")
            .moveAsSan("e4")
            .duration(1)
            .fen("rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1")
            .build();

    Move moveTwo =
        Move.builder()
            .moveAsUCI("d7d5")
            .moveAsSan("d5")
            .duration(1)
            .fen("rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 2")
            .build();

    Move moveThree =
        Move.builder()
            .moveAsUCI("e4d5")
            .moveAsSan("exd5")
            .duration(1)
            .fen("rnbqkbnr/ppp1pppp/8/3P4/8/8/PPPP1PPP/RNBQKBNR b KQkq - 0 2")
            .build();

    MakeMoveMessageData data =
        new MakeMoveMessageData(
            "rnbqkbnr/ppp1pppp/8/3P4/8/8/PPPP1PPP/RNBQKBNR b KQkq - 0 2",
            Arrays.asList(moveOne, moveTwo, moveThree),
            false,
            298,
            299);

    SocketResponseBody<MakeMoveMessageData> expectedResponse =
        new SocketResponseBody<>(WebsocketResponseAction.MOVE_MADE, data);
    assertEquals(expectedResponse.toJSON(), response.getBody());
  }

  @Test
  @DisplayName("Checkmate played")
  @Order(10)
  public void returnSuccessAndHandleCheckmate() throws NotFound {
    // setup board for a checkmate
    Game game = gameUtility.get(gameId);
    game.setGameStateAsFen(checkmateInOneFEN);
    game.setMoveList(new ArrayList<>());
    game.setIsWhitesTurn(true);

    gameUtility.put(gameId, game);

    MakeMoveRequest request =
        MakeMoveRequest.builder().gameId(gameId).playerId(userId).move(checkmateMove).build();

    APIGatewayV2WebSocketResponse response = getResponse(
        new MakeMoveHandler(makeMoveService, socketLogger),
        gson.toJson(request),
        makeRequestContext("makeMove", connectId)
    );

    assertResponse(response, StatusCodes.OK, "checkmate");

    // if everything went as planned, then the game was deleted and archived.
    // Therefore, recreate the game for the next test
    game.setId(gameId2);
    gameUtility.post(game);
  }

  @Test
  @DisplayName("Stalemate played")
  @Order(11)
  public void returnSuccessAndHandleStalemate() throws NotFound {

    // setup board for a stalemate
    Game game = gameUtility.get(gameId2);
    game.setIsWhitesTurn(true);
    game.setGameStateAsFen(stalemateInOneFEN);

    gameUtility.put(gameId2, game);

    MakeMoveRequest request =
        MakeMoveRequest.builder().gameId(gameId2).playerId(userId).move(stalemateMove).build();

    APIGatewayV2WebSocketResponse response = getResponse(
        new MakeMoveHandler(makeMoveService, socketLogger),
        gson.toJson(request),
        makeRequestContext("makeMove", connectId)
    );

    assertResponse(response, StatusCodes.OK, "draw");
  }
}
