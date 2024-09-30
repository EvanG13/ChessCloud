package org.example.models.requests;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.ToString;

@Getter
@Builder
@NonNull
@ToString
public class MakeMoveRequest {
  private final String action = "makeMove";
  @NonNull private String gameId;
  @NonNull private String playerId;
  @NonNull private String move;
}
