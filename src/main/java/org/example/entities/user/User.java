package org.example.entities.user;

import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.annotations.GsonExcludeField;
import org.example.entities.DataTransferObject;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class User extends DataTransferObject {
  private String email;
  @GsonExcludeField private String password;
  private String username;
  private Boolean verified;

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
            .append("\nverified = ")
            .append(verified);

    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    User user = (User) o;
    return Objects.equals(id, user.getId())
        && Objects.equals(email, user.getEmail())
        && Objects.equals(password, user.getPassword())
        && Objects.equals(username, user.getUsername())
        && Objects.equals(verified, user.getVerified());
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, password, email, username, verified);
  }
}
