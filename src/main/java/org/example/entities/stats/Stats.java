package org.example.entities.stats;

import com.google.gson.GsonBuilder;
import java.util.HashMap;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.annotations.CustomExclusionPolicy;
import org.example.annotations.GsonExcludeField;
import org.example.constants.ChessConstants;
import org.example.entities.DataTransferObject;
import org.example.enums.GameMode;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Stats extends DataTransferObject {
  private HashMap<String, GameModeStats> gameModeStats;

  public Stats(String userId) {
    this.id = userId;

    this.gameModeStats = new HashMap<>();
    for (GameMode gameMode : GameMode.values()) {
      // case insensitive
      gameModeStats.put(gameMode.asKey(), new GameModeStats());
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("userid = ").append(id);

    for (HashMap.Entry<String, GameModeStats> entry : gameModeStats.entrySet()) {
      GameModeStats gameModeStats = entry.getValue();
      sb.append("\n\tgamemode = ")
          .append(entry.getKey())
          .append("\n\t\twins   = ")
          .append(gameModeStats.getWins())
          .append("\n\t\tlosses = ")
          .append(gameModeStats.getLosses())
          .append("\n\t\tdraws  = ")
          .append(gameModeStats.getDraws())
          .append("\n\t\trating = ")
          .append(gameModeStats.getRating())
          .append("\n\t\tRD     = ")
          .append(gameModeStats.getRD());
    }

    return sb.toString();
  }

  public String toJSON() {
    return new GsonBuilder()
        .setExclusionStrategies(new CustomExclusionPolicy())
        .create()
        .toJson(gameModeStats);
  }

  public String toJSON(String gameMode) {
    return new GsonBuilder()
        .setExclusionStrategies(new CustomExclusionPolicy())
        .create()
        .toJson(getGamemodeStats(gameMode));
  }

  public String toJSON(GameMode gameMode) {
    return toJSON(gameMode.asKey());
  }

  public boolean doesGamemodeHaveStats(String gameMode) {
    return gameModeStats.containsKey(gameMode.toLowerCase());
  }

  public boolean doesGamemodeHaveStats(GameMode gameMode) {
    return doesGamemodeHaveStats(gameMode.asKey());
  }

  public GameModeStats getGamemodeStats(String gameMode) {
    return gameModeStats.get(gameMode.toLowerCase());
  }

  public GameModeStats getGamemodeStats(GameMode gameMode) {
    return getGamemodeStats(gameMode.asKey());
  }

  public int getRating(String gameMode) {
    return getGamemodeStats(gameMode).getRating();
  }

  public int getRating(GameMode gameMode) {
    return getRating(gameMode.asKey());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Stats stats = (Stats) o;
    return Objects.equals(id, stats.id) && Objects.equals(gameModeStats, stats.gameModeStats);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, gameModeStats);
  }

  @Getter
  @AllArgsConstructor
  public static class GameModeStats {
    // TODO: Move this to its own class?
    private Integer wins;
    private Integer losses;
    private Integer draws;
    private Integer rating;

    @GsonExcludeField
    private Double RD; // rating deviation used in Glicko rating system (what chess.com uses)

    public GameModeStats() {
      this(ChessConstants.BASE_RATING, ChessConstants.BASE_RD);
    }

    public GameModeStats(int rating) {
      this(rating, ChessConstants.BASE_RD);
    }

    public GameModeStats(int rating, double rd) {
      this.wins = 0;
      this.losses = 0;
      this.draws = 0;
      this.rating = rating;
      this.RD = rd;
      // relatively high uncertainty for new players allows new players to arrive
      // at their actual rating faster
    }

    // g is the adjusted impact of the opponent's rating taking into account their RD
    private double g(double opponentRd) {
      return 1.0
          / Math.sqrt(
              1
                  + 3
                      * ChessConstants.Q
                      * ChessConstants.Q
                      * opponentRd
                      * opponentRd
                      / (Math.PI * Math.PI));
    }

    private double expectedOutcome(double opponentRating, double opponentRd) {
      double gValue = g(opponentRd);
      return 1.0
          / (1.0 + Math.exp(-gValue * (this.rating - opponentRating) / (1.0 / ChessConstants.Q)));
    }

    public void AddWin(int opponentRating, double opponentRd) {
      this.wins++;
      double outcome = 1.0;
      updateRating(opponentRating, opponentRd, outcome);
    }

    public void AddLoss(int opponentRating, double opponentRd) {
      this.losses++;
      double outcome = 0.0;
      updateRating(opponentRating, opponentRd, outcome);
    }

    public void AddDraw(int opponentRating, double opponentRd) {
      this.draws++;
      double outcome = 0.5;
      updateRating(opponentRating, opponentRd, outcome);
    }

    private void updateRating(int opponentRating, double opponentRd, double outcome) {
      double gValue = g(opponentRd);
      double expectedOutcome = expectedOutcome(opponentRating, opponentRd);

      // Simplified Glicko-1 update equation
      double dSquared =
          1.0
              / (ChessConstants.Q
                  * ChessConstants.Q
                  * gValue
                  * gValue
                  * expectedOutcome
                  * (1 - expectedOutcome));
      double rdNew = 1.0 / Math.sqrt(1.0 / (RD * RD) + 1.0 / dSquared);

      double ratingChange =
          ChessConstants.Q
              / ((1.0 / (RD * RD)) + (1.0 / dSquared))
              * gValue
              * (outcome - expectedOutcome);
      this.rating += (int) Math.round(ratingChange);
      this.RD = rdNew;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      GameModeStats gameModeStats = (GameModeStats) o;
      return Objects.equals(wins, gameModeStats.wins)
          && Objects.equals(losses, gameModeStats.losses)
          && Objects.equals(draws, gameModeStats.draws)
          && Objects.equals(rating, gameModeStats.rating)
          && Objects.equals(RD, gameModeStats.RD);
    }

    @Override
    public int hashCode() {
      return Objects.hash(wins, losses, draws, rating, RD);
    }
  }
}
