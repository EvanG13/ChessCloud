package org.example.entities.stats;

import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.annotations.GsonExcludeField;
import org.example.constants.ChessConstants;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GameModeStats {
  private Integer wins;
  private Integer losses;
  private Integer draws;
  private Integer rating;

  @GsonExcludeField
  private Double RD; // rating deviation used in Glicko rating system (what chess.com uses)

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
  public String toString() {
    StringBuilder sb = new StringBuilder();

    sb.append("GameModeStats{");
    sb.append("wins=").append(wins);
    sb.append(", losses=").append(losses);
    sb.append(", draws=").append(draws);
    sb.append(", rating=").append(rating);
    sb.append(", RD=").append(RD);
    sb.append("}");

    return sb.toString();
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
