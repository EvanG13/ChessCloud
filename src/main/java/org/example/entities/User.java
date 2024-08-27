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
        && Objects.equals(username, user.getUsername());
    // this whole function could also just be: return this.toString() == o.toString() ?
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, password, email, username);
  }
}
