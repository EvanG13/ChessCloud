package org.example.annotations;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

import java.util.List;
import java.util.Set;

public class FieldExclusionStrategy implements ExclusionStrategy {

  private final Set<String> fieldsToExclude;
  public FieldExclusionStrategy( Set<String> excludedFields) {
    this.fieldsToExclude = excludedFields;
  }

  @Override
  public boolean shouldSkipField(FieldAttributes fieldAttributes) {
    return fieldsToExclude.contains(fieldAttributes.getName());
  }

  @Override
  public boolean shouldSkipClass(Class<?> clazz) {
    return false;
  }
}
