package org.example.entities;

import com.google.gson.annotations.Expose;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.bson.Document;

@Setter
@Getter
@AllArgsConstructor
public abstract class DataTransferObject {

  @Expose protected String id;

  /**
   * Convert object to a BSON Document
   *
   * @return BSON Document
   */
  public abstract Document toDocument();

  /**
   * Converts the Object to a Json string
   *
   * @return Json string
   */
  public abstract String toResponseJson();
}
