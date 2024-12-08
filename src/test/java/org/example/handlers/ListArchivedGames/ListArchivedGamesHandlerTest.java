package org.example.handlers.ListArchivedGames;

import static org.example.utils.HttpTestUtils.assertResponse;
import static org.example.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.example.constants.StatusCodes;
import org.example.entities.game.ArchivedGame;
import org.example.entities.game.ArchivedGameUtility;
import org.example.entities.game.Game;
import org.example.entities.player.Player;
import org.example.enums.GameStatus;
import org.example.enums.ResultReason;
import org.example.enums.TimeControl;
import org.example.handlers.rest.getArchivedGame.ListArchivedGamesHandler;
import org.example.models.responses.rest.ListArchivedGamesResponse;
import org.example.utils.MockContext;
import org.junit.jupiter.api.*;

public class ListArchivedGamesHandlerTest {
  private static ArchivedGameUtility archivedGameUtility;
  private static ListArchivedGamesResponse expectedWithOneGame;
  private static ListArchivedGamesResponse expectedWithTwoGames;
  private static ListArchivedGamesHandler handler;
  private static String gameId;
  private static String gameId2;

  @BeforeAll
  public static void setUp() throws Exception {
    archivedGameUtility = new ArchivedGameUtility();
    Game game = validGame(TimeControl.BLITZ_5);
    Game game2 = validGame(TimeControl.BLITZ_10);
    gameId = game.getId();
    gameId2 = game2.getId();
    game.setGameStatus(GameStatus.FINISHED);
    game2.setGameStatus(GameStatus.FINISHED);
    List<Player> players = new ArrayList<>();
    players.add(Player.builder().playerId("id1").username("user1").build());
    players.add(Player.builder().playerId("id2").username("user2").build());
    game.setPlayers(players);
    game2.setPlayers(players);
    ArchivedGame ar1 = archivedGameUtility.toArchivedGame(game, "user1", ResultReason.CHECKMATE);
    ArchivedGame ar2 = archivedGameUtility.toArchivedGame(game2, "user2", ResultReason.ABORTED);
    handler = new ListArchivedGamesHandler();
    List<ArchivedGame> archivedGameList = new ArrayList<>();
    List<ArchivedGame> archivedGameList2 = new ArrayList<>();
    archivedGameList.add(ar1);
    expectedWithOneGame = new ListArchivedGamesResponse(archivedGameList);
    archivedGameList2.add(ar1);
    archivedGameList2.add(ar2);
    expectedWithTwoGames = new ListArchivedGamesResponse(archivedGameList2);
    archivedGameUtility.post(ar1);
    archivedGameUtility.post(ar2);
  }

  @AfterAll
  public static void tearDown() {
    archivedGameUtility.delete(gameId);
    archivedGameUtility.delete(gameId2);
  }

  @Test
  public void canGetArchivedGamesWithoutTimeControl() {
    APIGatewayV2HTTPEvent event =
        APIGatewayV2HTTPEvent.builder()
            .withPathParameters(Map.of("username", "user1"))
            .withHeaders(Map.of("userid", "id1"))
            .build();

    APIGatewayV2HTTPResponse response = handler.handleRequest(event, new MockContext());
    assertResponse(response, StatusCodes.OK, expectedWithTwoGames.toJSON());
  }

  @Test
  public void canGetArchivedGamesWithTimeControl() {
    APIGatewayV2HTTPEvent event =
        APIGatewayV2HTTPEvent.builder()
            .withPathParameters(Map.of("username", "user1"))
            .withQueryStringParameters(Map.of("timeControl", String.valueOf(TimeControl.BLITZ_5)))
            .build();

    APIGatewayV2HTTPResponse response = handler.handleRequest(event, new MockContext());
    assertResponse(response, StatusCodes.OK, expectedWithOneGame.toJSON());
  }

  @Test
  public void missingPathParamsSendsBADREQUEST() {
    APIGatewayV2HTTPEvent event =
        APIGatewayV2HTTPEvent.builder().withHeaders(Map.of("userid", "id2")).build();

    APIGatewayV2HTTPResponse response = handler.handleRequest(event, new MockContext());
    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  public void userWithNoArchivedGamesReturnsEmpty() {
    APIGatewayV2HTTPEvent event =
        APIGatewayV2HTTPEvent.builder()
            .withPathParameters(Map.of("username", "user3"))
            .withHeaders(Map.of("userid", "new-user"))
            .build();

    APIGatewayV2HTTPResponse response = handler.handleRequest(event, new MockContext());
    assertResponse(response, StatusCodes.OK, "{\"archivedGames\":[]}");
  }

  @Test
  public void invalidQueryParams() {
    APIGatewayV2HTTPEvent event =
        APIGatewayV2HTTPEvent.builder()
            .withPathParameters(Map.of("username", "user1"))
            .withHeaders(Map.of("userid", "id1"))
            .withQueryStringParameters(Map.of("fdsaf", "invalid-queryParamKey!"))
            .build();

    APIGatewayV2HTTPResponse response = handler.handleRequest(event, new MockContext());
    assertResponse(
        response, StatusCodes.BAD_REQUEST, "Bad query param. Expected either none or timeControl");
  }
}
