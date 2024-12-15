package org.example.entities.timeControl;

import java.util.Objects;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class TimeControl {
  @Builder.Default Integer base = 180;
  @Builder.Default Integer increment = 0;

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
    return Objects.equals(this.base, timeControl.base)
        && Objects.equals(this.increment, timeControl.increment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(base, increment);
  }
}
