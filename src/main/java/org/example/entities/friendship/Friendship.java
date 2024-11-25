package org.example.entities.friendship;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.example.entities.DataTransferObject;
import org.example.enums.FriendshipStatus;

import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class Friendship extends DataTransferObject {
  private String senderUserId;
  private String receiverUserId;
  private FriendshipStatus status;

  @Override
  public String toString() {
    StringBuilder sb =
        new StringBuilder()
            .append("id             = ")
            .append(id)
            .append("\nsenderUserId   = ")
            .append(senderUserId)
            .append("\nreceiverUserId = ")
            .append(receiverUserId)
            .append("\nstatus         = ")
            .append(status);

    return sb.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Friendship object = (Friendship) o;
    return Objects.equals(id, object.getId())
        && Objects.equals(senderUserId, object.getSenderUserId())
        && Objects.equals(receiverUserId, object.getReceiverUserId())
        && Objects.equals(status, object.getStatus());
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, senderUserId, receiverUserId, status);
  }
}
