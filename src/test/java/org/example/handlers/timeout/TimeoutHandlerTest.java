package org.example.handlers.timeout;

import static org.example.utils.TestUtils.*;
import static org.example.utils.WebsocketTestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.example.constants.ChessConstants;
import org.example.constants.StatusCodes;
import org.example.entities.game.ArchivedGame;
import org.example.entities.game.ArchivedGameDbService;
import org.example.entities.game.Game;
import org.example.entities.game.GameDbService;
import org.example.entities.player.ArchivedPlayer;
import org.example.entities.player.Player;
import org.example.entities.player.PlayerDbService;
import org.example.entities.stats.StatsDbService;
import org.example.entities.user.User;
import org.example.entities.user.UserDbService;
import org.example.enums.GameStatus;
import org.example.enums.ResultReason;
import org.example.enums.TimeControl;
import org.example.exceptions.NotFound;
import org.example.handlers.websocket.timeout.TimeoutHandler;
import org.example.handlers.websocket.timeout.TimeoutService;
import org.example.models.requests.TimeoutRequest;
import org.example.utils.socketMessenger.SocketSystemLogger;
import org.junit.jupiter.api.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TimeoutHandlerTest {
  private static ArchivedGameDbService archivedGameDbService;
  private static GameDbService gameDbService;
  private static UserDbService userDbService;
  private static StatsDbService statsDbService;
  private static TimeoutHandler handler;
  private static Game game;
  private static User userOne;
  private static User userTwo;
  private static Game game2;

  @BeforeAll
  public static void setUp() throws Exception {
    handler = new TimeoutHandler(new TimeoutService(), new SocketSystemLogger());

    archivedGameDbService = ArchivedGameDbService.builder().build();
    gameDbService = new GameDbService();
    userDbService = new UserDbService();
    PlayerDbService playerDbService = new PlayerDbService();
    statsDbService = new StatsDbService();
    userOne = validUser();
    userTwo = validUser();

    Player playerOne = playerDbService.toPlayer(userOne, ChessConstants.BASE_RATING, "whatever", true);
    Player playerTwo =
        playerDbService.toPlayer(userTwo, ChessConstants.BASE_RATING, "secondWhatever", false);
    playerOne.setRemainingTime(100);
    playerTwo.setRemainingTime(12);

    game =
        Game.builder()
            .moveList(new ArrayList<>())
            .players(List.of(playerOne, playerTwo))
            .gameStatus(GameStatus.ONGOING)
            .timeControl(TimeControl.BLITZ_5)
            .lastModified(new Date())
            .isWhitesTurn(true)
            .build();
    gameDbService.post(game);

    game2 =
        Game.builder()
            .moveList(new ArrayList<>())
            .players(List.of(playerOne, playerTwo))
            .gameStatus(GameStatus.ONGOING)
            .timeControl(TimeControl.BLITZ_10)
            .lastModified(new Date())
            .isWhitesTurn(true)
            .build();
    gameDbService.post(game2);
  }

  @Test
  @Order(1)
  public void checkNonPlayerUserTriedTimeoutRequest() {
    APIGatewayV2WebSocketResponse response = getResponse(
        handler,
        new TimeoutRequest(game.getId()),
        makeRequestContext("timeout", "some-other-guy")
    );

    assertResponse(response, StatusCodes.UNAUTHORIZED, "Your connection ID is not bound to this game");
  }

  @Test
  @Order(2)
  public void falseTimeoutReturnsNotFound() {
    APIGatewayV2WebSocketResponse response = getResponse(
        handler,
        new TimeoutRequest(game.getId()),
        makeRequestContext("timeout", "whatever")
    );

    assertEquals(StatusCodes.NOT_FOUND, response.getStatusCode());
  }

  @Test
  @Order(4)
  public void canTimeoutGame() {
    List<Player> players = game.getPlayers();
    players.getFirst().setRemainingTime(-1);
    players.getLast().setRemainingTime(21);
    gameDbService.put(game.getId(), game);

    String winningPlayerId = players.getLast().getPlayerId();

    APIGatewayV2WebSocketResponse response = getResponse(
        handler,
        new TimeoutRequest(game.getId()),
        makeRequestContext("timeout", "whatever")
    );

    assertEquals(StatusCodes.OK, response.getStatusCode());

    ArchivedGame archivedGame;
    try {
      archivedGame = archivedGameDbService.getArchivedGame(game.getId());
    } catch (NotFound e) {
      fail("Game was not successfully archived");
      return;
    }

    assertEquals(ResultReason.TIMEOUT, archivedGame.getResultReason());

    ArchivedPlayer winningPlayer = archivedGame.getPlayers().getLast();
    assertEquals(true, winningPlayer.getIsWinner());
    assertEquals(winningPlayerId, winningPlayer.getPlayerId());
  }

  @Test
  @Order(3)
  public void bothPlayersAtZeroReturns500() {
    List<Player> players = game.getPlayers();
    players.getFirst().setRemainingTime(-1);
    players.getLast().setRemainingTime(0);
    gameDbService.put(game.getId(), game);

    APIGatewayV2WebSocketResponse response = getResponse(
        handler,
        new TimeoutRequest(game.getId()),
        makeRequestContext("timeout", "whatever")
    );

    assertEquals(StatusCodes.INTERNAL_SERVER_ERROR, response.getStatusCode());
  }

  @Test
  @Order(5)
  public void canOtherPlayerTimeoutGame() {
    List<Player> players = game2.getPlayers();
    players.getFirst().setRemainingTime(100); // winning player
    players.getLast().setRemainingTime(0); // losing player

    gameDbService.put(game2.getId(), game2);

    String winningPlayerId = players.getFirst().getPlayerId();

    APIGatewayV2WebSocketResponse response = getResponse(
        handler,
        new TimeoutRequest(game2.getId()),
        makeRequestContext("timeout", "whatever")
    );

    assertEquals(StatusCodes.OK, response.getStatusCode());

    ArchivedGame archivedGame;
    try {
      archivedGame = archivedGameDbService.getArchivedGame(game2.getId());
    } catch (NotFound e) {
      fail("Game was not successfully archived");
      return;
    }

    assertEquals(ResultReason.TIMEOUT, archivedGame.getResultReason());

    ArchivedPlayer winningPlayer = archivedGame.getPlayers().getFirst();
    assertEquals(true, winningPlayer.getIsWinner());
    assertEquals(winningPlayerId, winningPlayer.getPlayerId());
  }

  @Test
  @Order(6)
  public void checksForMissingBody() {
    APIGatewayV2WebSocketResponse response = getResponse(
        handler,
        Map.of("foo", "fooagain"),
        makeRequestContext("timeout", "foo-id")
    );

    assertResponse(response, StatusCodes.BAD_REQUEST, "Missing argument(s)");
  }

  @Test
  @Order(7)
  public void checksThatGameExists() {
    String fakeID = "fake";

    APIGatewayV2WebSocketResponse response = getResponse(
        handler,
        new TimeoutRequest(fakeID),
        makeRequestContext("timeout", "foo-id")
    );

    assertResponse(response, StatusCodes.NOT_FOUND, "No Game found with id " + fakeID);
  }

  @AfterAll
  public static void tearDown() {
    archivedGameDbService.deleteArchivedGame(game.getId());
    archivedGameDbService.deleteArchivedGame(game2.getId());

    userDbService.deleteUser(userOne.getId());
    userDbService.deleteUser(userTwo.getId());

    statsDbService.deleteStats(userOne.getId());
    statsDbService.deleteStats(userTwo.getId());
  }
}
