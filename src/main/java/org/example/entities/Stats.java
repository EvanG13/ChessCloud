package org.example.entities;

import com.google.gson.annotations.Expose;
import java.util.HashMap;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.utils.GameMode;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Stats extends DataTransferObject {
  @Expose private HashMap<String, GameModeStats> gameModeStats;

  public Stats(String userId) {
    this.id = userId;

    this.gameModeStats = new HashMap<>();
    for (GameMode gameMode : GameMode.values()) {
      gameModeStats.put(gameMode.toString(), new GameModeStats());
    }
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("userid = ").append(id);

    for (HashMap.Entry<String, GameModeStats> entry : gameModeStats.entrySet()) {
      sb.append("\n\tgamemode = ").append(entry.getKey());
      sb.append("\n\t\twins   = ").append(entry.getValue().getWins());
      sb.append("\n\t\tlosses = ").append(entry.getValue().getLosses());
      sb.append("\n\t\tdraws  = ").append(entry.getValue().getDraws());
      sb.append("\n\t\trating = ").append(entry.getValue().getRating());
    }

    return sb.toString();
  }

  public int getRating(GameMode gameMode) {
    return gameModeStats.get(gameMode.toString()).getRating();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Stats stats = (Stats) o;
    return Objects.equals(id, stats.id)
        && Objects.equals(gameModeStats, stats.gameModeStats);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, gameModeStats);
  }

  @Getter
  @AllArgsConstructor
  public static class GameModeStats {
    private Integer wins;
    private Integer losses;
    private Integer draws;
    private Integer rating;

    public GameModeStats() {
      this.wins = 0;
      this.losses = 0;
      this.draws = 0;
      this.rating = 1000; // starting rating - get from const somewhere instead?
    }

    public GameModeStats(int rating) {
      this.wins = 0;
      this.losses = 0;
      this.draws = 0;
      this.rating = rating;
    }

    public void AddWin(int opponentRating) {
      this.wins++;
      // determine rating gain?
    }

    public void AddLoss(int opponentRating) {
      this.losses++;
      // determine rating loss?
    }

    public void AddDraw() {
      this.draws++;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      GameModeStats gameModeStats = (GameModeStats) o;
      return Objects.equals(wins, gameModeStats.wins)
          && Objects.equals(losses, gameModeStats.losses)
          && Objects.equals(draws, gameModeStats.draws)
          && Objects.equals(rating, gameModeStats.rating);
    }

    @Override
    public int hashCode() {
      return Objects.hash(wins, losses, draws, rating);
    }
  }
}
