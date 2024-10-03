package org.example.entities.session;

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
public class Session extends DataTransferObject {
  private String userId;

  public String toString() {
    return this.id + " " + this.userId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Session session = (Session) o;

    return Objects.equals(id, session.getId()) && Objects.equals(userId, session.getUserId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, userId);
  }
}
