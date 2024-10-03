package org.example.annotations;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class CustomExclusionPolicy implements ExclusionStrategy {
  @Override
  public boolean shouldSkipField(FieldAttributes fieldAttributes) {
    return fieldAttributes.getAnnotation(GsonExcludeField.class) != null;
  }

  @Override
  public boolean shouldSkipClass(Class<?> aClass) {
    return false;
  }
}
