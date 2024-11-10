package org.example.entities.connection;

import com.google.gson.annotations.Expose;
import java.util.Objects;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.entities.DataTransferObject;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Connection extends DataTransferObject {
  @Expose private String username;

  @Override
  public String toString() {
    return this.username + " " + id;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Connection connection = (Connection) o;
    return Objects.equals(id, connection.getId())
        && Objects.equals(username, connection.getUsername());
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, username);
  }
}
