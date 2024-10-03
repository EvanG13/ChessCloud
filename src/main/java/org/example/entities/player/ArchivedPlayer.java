package org.example.entities.player;

import lombok.experimental.SuperBuilder;

@SuperBuilder
public class ArchivedPlayer extends BasePlayer {
  Boolean isWinner;
}
