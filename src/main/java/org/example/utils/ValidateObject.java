package org.example.utils;

import java.lang.reflect.Field;
import java.util.Objects;
import lombok.NonNull;

public class ValidateObject {
  private ValidateObject() {}

  public static <T> void requireNonNull(@NonNull T object) throws NullPointerException {
    Field[] fields = object.getClass().getDeclaredFields();

    for (Field field : fields) {
      field.setAccessible(true); // Access private fields

      try {
        Object value = field.get(object);
        Objects.requireNonNull(value);
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Could not access field: " + field.getName(), e);
      }
    }
  }

  public static boolean isAnyFieldInObjectNull(Object object) {
    if (object == null) return true;

    Field[] fields = object.getClass().getDeclaredFields();

    for (Field field : fields) {
      field.setAccessible(true); // Access private fields

      try {
        Object value = field.get(object);
        Objects.requireNonNull(value);
      } catch (IllegalAccessException e) {
        throw new RuntimeException("Could not access field: " + field.getName(), e);
      } catch (NullPointerException e) {
        return true;
      }
    }

    return false;
  }
}
