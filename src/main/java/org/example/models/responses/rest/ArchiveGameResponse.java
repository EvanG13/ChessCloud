package org.example.models.responses.rest;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.example.entities.move.Move;
import org.example.entities.player.ArchivedPlayer;
import org.example.entities.timeControl.TimeControl;
import org.example.enums.ResultReason;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveGameResponse extends ResponseBody {
  private TimeControl timeControl;
  private Date created;
  private List<Move> moveList;
  private List<ArchivedPlayer> players;
  private Integer rating;
  private ResultReason resultReason;
}
