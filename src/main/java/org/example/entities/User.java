package org.example.entities;

import com.google.gson.annotations.Expose;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class User extends DataTransferObject {
  @Expose private String email;

  private Integer rating;
  private Integer gamesWon;
  private Integer gamesLost;
  private String password;
  @Expose private String username;

  @Override
  public String toString() {
    StringBuilder sb =
        new StringBuilder()
            .append("id       = ")
            .append(id)
            .append("\nusername = ")
            .append(username)
            .append("\nemail    = ")
            .append(email)
            .append("\nwins     = ")
            .append(gamesWon)
            .append("\nlost     = ")
            .append(gamesLost);

    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    User user = (User) o;
    return Objects.equals(id, user.getId())
        && Objects.equals(email, user.getEmail())
        && Objects.equals(password, user.getPassword())
        && Objects.equals(rating, user.getRating())
        && Objects.equals(gamesWon, user.getGamesWon())
        && Objects.equals(gamesLost, user.getGamesLost())
        && Objects.equals(username, user.getUsername());
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, password, email, username);
  }
}
