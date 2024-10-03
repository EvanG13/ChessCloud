package org.example.entities.move;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Move {
  String moveAsUCI;
  String moveAsSan;
  Integer duration;
}
