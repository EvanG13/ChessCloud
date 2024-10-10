package org.example.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import org.bson.types.ObjectId;
import org.example.constants.ChessConstants;
import org.example.entities.game.Game;
import org.example.entities.player.Player;
import org.example.entities.player.PlayerDbService;
import org.example.entities.stats.Stats;
import org.example.entities.stats.StatsDbService;
import org.example.entities.user.User;
import org.example.entities.user.UserDbService;
import org.example.enums.TimeControl;

public class TestUtils {

  private static final UserDbService userDbService = new UserDbService();
  private static final StatsDbService statsDbService = new StatsDbService();
  private static final PlayerDbService playerDbService = new PlayerDbService();

  public static void assertCorsHeaders(Map<String, String> headers) {
    assertEquals(
        "*", headers.get("Access-Control-Allow-Origin"), "Incorrect CORS header for Allow-Origin");
    assertEquals(
        "POST,OPTIONS",
        headers.get("Access-Control-Allow-Methods"),
        "Incorrect CORS header for Allow-Methods");
    assertEquals(
        "*",
        headers.get("Access-Control-Allow-Headers"),
        "Incorrect CORS header for Allow-Headers");
  }

  public static Player validPlayer(String username, boolean isWhite) {
    return Player.builder()
        .remainingTime(0)
        .connectionId(new ObjectId().toString())
        .playerId(new ObjectId().toString())
        .username(username)
        .rating(ChessConstants.BASE_RATING)
        .isWhite(isWhite)
        .build();
  }

  public static Game validGame(TimeControl timeControl) throws Exception {
    Player white = validPlayer("white", true);
    Player black = validPlayer("black", false);
    Game game = new Game(timeControl, white);

    game.setup(black);

    return game;
  }

  public static Game validGame(TimeControl timeControl, User playerOne, User playerTwo)
      throws Exception {
    Game game = new Game(timeControl, playerDbService.toPlayer(playerOne, "foo-id", false));

    game.setup(playerDbService.toPlayer(playerTwo, "foo-id-again", false));

    return game;
  }

  public static User validUser() {
    User user =
        User.builder().email("foo-email").username("foo-username").password("foo-password").build();

    Stats stats = new Stats(user.getId());

    userDbService.createUser(user);
    statsDbService.post(stats);

    return user;
  }
}
