package org.example.entities;

import com.google.gson.annotations.Expose;
import org.bson.Document;

public abstract class DataTransferObject {

  @Expose protected String id;

  public DataTransferObject(String id) {
    this.id = id;
  }

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

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
