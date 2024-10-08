package org.example.handlers.rest.getArchivedGame;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.example.entities.game.ArchivedGame;
import org.example.entities.game.ArchivedGameDbService;
import org.example.exceptions.NotFound;

@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetArchivedGameService {
  @Builder.Default
  private final ArchivedGameDbService archivedGameDbService =
      ArchivedGameDbService.builder().build();

  ArchivedGame getArchivedGame(String id) throws NotFound {
    return archivedGameDbService.getArchivedGame(id);
  }
}
