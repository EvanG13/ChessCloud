package org.example.entities.token;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.entities.DataTransferObject;

import java.util.Date;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class BaseToken extends DataTransferObject {
  private String userId;
  private Date expiresAt;

  public boolean isExpired() {
    return new Date().after(this.expiresAt);
  }

  @Override
  public String toString() {
    StringBuilder sb =
        new StringBuilder()
            .append("id        = ")
            .append(id)
            .append("\nuserId    = ")
            .append(userId)
            .append("\nexpiresAt = ")
            .append(expiresAt);

    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    BaseToken object = (BaseToken) o;
    return Objects.equals(id, object.getId())
        && Objects.equals(userId, object.getUserId())
        && Objects.equals(expiresAt, object.getExpiresAt());
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, userId, expiresAt);
  }
}
