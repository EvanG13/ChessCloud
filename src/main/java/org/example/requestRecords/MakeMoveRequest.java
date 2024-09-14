package org.example.requestRecords;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MakeMoveRequest {
  private String gameId;
  private String playerId;
  private String move;
  private String connectionId;
  private String boardState;
}
