package org.example.models.responses.rest;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MakeMoveResponseBody extends ResponseBody {
  private String fen;
  private List<String> moveList;
  private final String action = "MOVE_MADE";
}
