package org.example.handlers.timeout;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2WebSocketResponse;
import com.google.gson.Gson;
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
import org.example.handlers.websocket.resign.ResignGameHandler;
import org.example.handlers.websocket.resign.ResignGameService;
import org.example.handlers.websocket.timeout.TimeoutHandler;
import org.example.handlers.websocket.timeout.TimeoutService;
import org.example.models.requests.ResignRequest;
import org.example.models.requests.TimeoutRequest;
import org.example.utils.MockContext;
import org.example.utils.socketMessenger.SocketSystemLogger;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.example.utils.TestUtils.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TimeoutHandlerTest {
    private static ArchivedGameDbService archivedGameDbService;
    private static UserDbService userDbService;
    private static StatsDbService statsDbService;
    private static PlayerDbService playerDbService;
    private static TimeoutHandler handler;
    private static Game game;
    private static Player playerOne;
    private static Player playerTwo;
    private static User userOne;
    private static User userTwo;

    @BeforeAll
    public static void setUp() throws Exception {
        handler = new TimeoutHandler(new TimeoutService(), new SocketSystemLogger());

        archivedGameDbService = ArchivedGameDbService.builder().build();
        GameDbService gameDbService = new GameDbService();
        userDbService = new UserDbService();
        playerDbService = new PlayerDbService();
        statsDbService = new StatsDbService();
        userOne = validUser();
        userTwo = validUser();
        playerOne = playerDbService.toPlayer(userOne, "whatever", true);
        playerTwo = playerDbService.toPlayer(userTwo, "secondWhatever", false);
        playerOne.setRemainingTime(-1);
        playerTwo.setRemainingTime(12);

        game = Game.builder().
                moveList(new ArrayList<>()).
                players(List.of(playerOne, playerTwo)).
                gameStatus(GameStatus.ONGOING).timeControl(TimeControl.BLITZ_5).
                build();
        gameDbService.post(game);
    }

    @Test
    @Order(1)
    public void checkNonPlayerUserTriedTimeoutRequest() {
        APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
        event.setBody(new Gson().toJson(Map.of("gameId", game.getId())));

        APIGatewayV2WebSocketEvent.RequestContext requestContext =
                new APIGatewayV2WebSocketEvent.RequestContext();
        requestContext.setConnectionId("some-other-guy");
        requestContext.setRouteKey("timeout");
        event.setRequestContext(requestContext);

        APIGatewayV2WebSocketResponse response = handler.handleRequest(event, new MockContext());

        assertEquals(StatusCodes.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Your connection ID is not bound to this game", response.getBody());
    }

    @Test
    @Order(2)
    public void canTimeoutGame() {
        List<Player> players = game.getPlayers();

        String winningPlayerId = players.getLast().getPlayerId();

        APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
        TimeoutRequest request = new TimeoutRequest(game.getId());
        event.setBody(new Gson().toJson(request));

        APIGatewayV2WebSocketEvent.RequestContext requestContext =
                new APIGatewayV2WebSocketEvent.RequestContext();
        requestContext.setConnectionId("whatever");
        requestContext.setRouteKey("timeout");
        event.setRequestContext(requestContext);

        APIGatewayV2WebSocketResponse response = handler.handleRequest(event, new MockContext());

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
    public void checksForMissingBody() {
        APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
        event.setBody(new Gson().toJson(Map.of("foo", "fooagain")));

        APIGatewayV2WebSocketEvent.RequestContext requestContext =
                new APIGatewayV2WebSocketEvent.RequestContext();
        requestContext.setConnectionId("foo-id");
        requestContext.setRouteKey("timeout");
        event.setRequestContext(requestContext);

        APIGatewayV2WebSocketResponse response = handler.handleRequest(event, new MockContext());

        assertEquals(StatusCodes.BAD_REQUEST, response.getStatusCode());
        assertEquals("Missing argument(s)", response.getBody());
    }

    @Test
    public void checksThatGameExists() {
        APIGatewayV2WebSocketEvent event = new APIGatewayV2WebSocketEvent();
        String fakeID = "fake";
        event.setBody(new Gson().toJson(Map.of("gameId", fakeID)));

        APIGatewayV2WebSocketEvent.RequestContext requestContext =
                new APIGatewayV2WebSocketEvent.RequestContext();
        requestContext.setConnectionId("foo-id");
        requestContext.setRouteKey("timeout");
        event.setRequestContext(requestContext);

        APIGatewayV2WebSocketResponse response = handler.handleRequest(event, new MockContext());

        assertEquals(StatusCodes.NOT_FOUND, response.getStatusCode());
        assertEquals("No Game found with id " + fakeID, response.getBody());
    }

    @AfterAll
    public static void tearDown() {
        archivedGameDbService.deleteArchivedGame(game.getId());

        userDbService.deleteUser(userOne.getId());
        userDbService.deleteUser(userTwo.getId());

        statsDbService.deleteStats(userOne.getId());
        statsDbService.deleteStats(userTwo.getId());
    }
}

