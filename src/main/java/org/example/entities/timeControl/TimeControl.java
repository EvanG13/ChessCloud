package org.example.entities.timeControl;

import lombok.*;

import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TimeControl {
  int base;
  int increment;

  @Override
  public String toString() {
    return String.format("TimeControl {base=%s, increment=%s}", base, increment);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;

    TimeControl timeControl = (TimeControl) o;
    return this.base == timeControl.base
        && this.increment == timeControl.increment;
  }

  @Override
  public int hashCode() {
    return Objects.hash(base, increment);
  }
}