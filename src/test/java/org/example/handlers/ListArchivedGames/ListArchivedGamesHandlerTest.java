package org.example.handlers.ListArchivedGames;

import static org.example.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.example.constants.StatusCodes;
import org.example.entities.game.ArchivedGame;
import org.example.entities.game.ArchivedGameDbService;
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
  private static ArchivedGameDbService archivedGameDbService;
  private static ListArchivedGamesResponse expectedWithOneGame;
  private static ListArchivedGamesResponse expectedWithTwoGames;
  private static ListArchivedGamesHandler handler;
  private static String gameId;
  private static String gameId2;

  @BeforeAll
  public static void setUp() throws Exception {
    archivedGameDbService = ArchivedGameDbService.builder().build();
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
    ArchivedGame ar1 = archivedGameDbService.toArchivedGame(game, "user1", ResultReason.CHECKMATE);
    ArchivedGame ar2 = archivedGameDbService.toArchivedGame(game2, "user2", ResultReason.ABORTED);
    handler = new ListArchivedGamesHandler();
    List<ArchivedGame> archivedGameList = new ArrayList<>();
    List<ArchivedGame> archivedGameList2 = new ArrayList<>();
    archivedGameList.add(ar1);
    expectedWithOneGame = new ListArchivedGamesResponse(archivedGameList);
    archivedGameList2.add(ar1);
    archivedGameList2.add(ar2);
    expectedWithTwoGames = new ListArchivedGamesResponse(archivedGameList2);
    archivedGameDbService.archiveGame(ar1);
    archivedGameDbService.archiveGame(ar2);
  }

  @AfterAll
  public static void tearDown() {
    archivedGameDbService.deleteArchivedGame(gameId);
    archivedGameDbService.deleteArchivedGame(gameId2);
  }

  @Test
  public void canGetArchivedGamesWithoutTimeControl() {
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();

    Map<String, String> pathParams = new HashMap<>();
    pathParams.put("username", "user1");
    Map<String, String> headerMap = new HashMap<>();
    headerMap.put("userid", "id1");
    event.setPathParameters(pathParams);
    event.setHeaders(headerMap);
    APIGatewayV2HTTPResponse response = handler.handleRequest(event, new MockContext());
    assertEquals(StatusCodes.OK, response.getStatusCode());

    String actual = response.getBody();
    assertEquals(expectedWithTwoGames.toJSON(), actual);
  }

  @Test
  public void canGetArchivedGamesWithTimeControl() {
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();

    Map<String, String> pathParams = new HashMap<>();
    pathParams.put("username", "id1");
    Map<String, String> queryParams = new HashMap<>();
    queryParams.put("timeControl", String.valueOf(TimeControl.BLITZ_5));
    event.setPathParameters(pathParams);
    event.setQueryStringParameters(queryParams);
    APIGatewayV2HTTPResponse response = handler.handleRequest(event, new MockContext());
    assertEquals(StatusCodes.OK, response.getStatusCode());

    String actual = response.getBody();
    assertEquals(expectedWithOneGame.toJSON(), actual);
  }

  //  @Test
  //  public void missingPathParamsUseHeadersInstead() {
  //    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
  //    Map<String, String> headerMap = new HashMap<>();
  //    headerMap.put("userid", "id2");
  //    event.setHeaders(headerMap);
  //    APIGatewayV2HTTPResponse response = handler.handleRequest(event, new MockContext());
  //    assertEquals(StatusCodes.OK, response.getStatusCode());
  //    String actual = response.getBody();
  //    assertEquals(expectedWithTwoGames.toJSON(), actual);
  //  }

  @Test
  public void missingPathParamsSendsBADREQUEST() {
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
    Map<String, String> headerMap = new HashMap<>();
    headerMap.put("userid", "id2");
    event.setHeaders(headerMap);
    APIGatewayV2HTTPResponse response = handler.handleRequest(event, new MockContext());
    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
  }

  @Test
  public void userWithNoArchivedGamesReturnsEmpty() {
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
    Map<String, String> path = new HashMap<>();
    path.put("username", "user3");
    Map<String, String> headerMap = new HashMap<>();
    headerMap.put("userid", "new-user");
    event.setHeaders(headerMap);
    event.setPathParameters(path);
    APIGatewayV2HTTPResponse response = handler.handleRequest(event, new MockContext());
    assertEquals(StatusCodes.OK, response.getStatusCode());
    String actual = response.getBody();
    assertEquals("{\"archivedGames\":[]}", actual);
  }

  @Test
  public void invalidQueryParams() {
    APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent();
    Map<String, String> path = new HashMap<>();
    path.put("username", "user1");
    Map<String, String> map = new HashMap<>();
    map.put("fdsaf", "invalid-queryParamKey!");
    event.setQueryStringParameters(map);
    event.setPathParameters(path);
    Map<String, String> headerMap = Map.of("userid", "id1");
    event.setHeaders(headerMap);
    APIGatewayV2HTTPResponse response = handler.handleRequest(event, new MockContext());
    assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
    assertEquals("Bad query param. Expected either none or timeControl", response.getBody());
  }
}
