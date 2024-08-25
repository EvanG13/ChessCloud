package org.example.entities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
public class Connection extends DataTransferObject {
  @Expose private String username;

  @Override
  public String toResponseJson() {
    Gson gsonBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    return gsonBuilder.toJson(this, Connection.class);
  }

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
