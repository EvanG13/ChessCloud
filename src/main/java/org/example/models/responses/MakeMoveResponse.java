package org.example.models.responses;

import com.google.gson.annotations.Expose;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MakeMoveResponse extends Response {
  @Expose private String fen;
  @Expose private List<String> moveList;
}
