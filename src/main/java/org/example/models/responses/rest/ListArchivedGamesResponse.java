package org.example.models.responses.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.example.annotations.FieldExclusionStrategy;
import org.example.entities.game.ArchivedGame;

@Getter
@AllArgsConstructor
public class ListArchivedGamesResponse extends ResponseBody {
  private List<ArchivedGame> archivedGames;

  @Override
  public String toJSON() {
    Set<String> fieldsToExclude = Set.of("moveList");
    Gson gsonWithoutMoveList =
        new GsonBuilder()
            .setExclusionStrategies(new FieldExclusionStrategy(fieldsToExclude))
            .create();
    return gsonWithoutMoveList.toJson(this);
  }
}
