package org.example.entities;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.example.annotations.CustomExclusionPolicy;

@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class DataTransferObject {

  @BsonProperty(value = "_id")
  @Builder.Default
  protected String id = new ObjectId().toString();

  /**
   * Converts the Object to a Json string
   *
   * @return Json string
   */
  public String toResponseJson() {
    Gson gsonBuilder =
        new GsonBuilder().setExclusionStrategies(new CustomExclusionPolicy()).create();

    return gsonBuilder.toJson(this, getClass());
  }

  @Override
  public abstract String toString();

  @Override
  public abstract boolean equals(Object o);

  @Override
  public abstract int hashCode();
}
