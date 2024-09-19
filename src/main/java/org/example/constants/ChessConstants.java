package org.example.constants;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class ChessConstants {
  public static final Integer BASE_RATING = 1000;
  public static final Double BASE_RD = 350.0;
  public static final Double Q =
      Math.log(10) / 400; // scaling factor constant used in calculating glicko ratings
  public static final String STARTING_FEN_STRING =
      "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";
}
