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

  @Expose private Integer rating;
  @Expose private Integer gamesWon;
  @Expose private Integer gamesLost;
  private String password;
  @Expose private String username;

  @Override
  public String toString() {
    return email + " " + username + " " + id;
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
