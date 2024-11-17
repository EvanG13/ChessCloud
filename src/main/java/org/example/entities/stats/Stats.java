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
      gameModeStats.put(
          gameMode.asKey(), new GameModeStats(ChessConstants.BASE_RATING, ChessConstants.BASE_RD));
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
    return Objects.hash(id, Objects.hash(gameModeStats));
  }
}
