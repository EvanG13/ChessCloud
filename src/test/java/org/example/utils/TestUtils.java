package org.example.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;
import java.util.Random;
import org.bson.types.ObjectId;
import org.example.constants.ChessConstants;
import org.example.entities.game.Game;
import org.example.entities.player.Player;
import org.example.entities.player.PlayerUtility;
import org.example.entities.stats.Stats;
import org.example.entities.stats.StatsUtility;
import org.example.entities.timeControl.TimeControl;
import org.example.entities.user.User;
import org.example.entities.user.UserUtility;

public class TestUtils {

  private static final UserUtility userUtility = new UserUtility();
  private static final StatsUtility statsUtility = new StatsUtility();

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
    Random random = new Random();

    Game game =
        new Game(
            timeControl,
            PlayerUtility.toPlayer(
                playerOne, ChessConstants.BASE_RATING, "foo-id-" + random.nextInt(), false));

    game.setup(
        PlayerUtility.toPlayer(
            playerTwo, ChessConstants.BASE_RATING, "foo-id-again-" + random.nextInt(), false));

    return game;
  }

  public static User validUser() {
    int randomInt = new Random().nextInt();

    User user =
        User.builder()
            .email("foo-email-" + randomInt)
            .username("foo-username-" + randomInt)
            .password("foo-password")
            .build();

    Stats stats = new Stats(user.getId());

    userUtility.post(user);
    statsUtility.post(stats);

    return user;
  }
}
