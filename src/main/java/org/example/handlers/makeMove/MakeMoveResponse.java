package org.example.handlers.makeMove;

import com.google.gson.Gson;
import java.util.List;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MakeMoveResponse {
  private String fen;
  private List<String> moveList;

  public String toJSON() {
    Gson gson = new Gson();
    return gson.toJson(this);
  }
}
