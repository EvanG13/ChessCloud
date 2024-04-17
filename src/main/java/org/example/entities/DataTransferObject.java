package org.example.entities;

import org.bson.Document;

public abstract class DataTransferObject {
  protected String id;

  public DataTransferObject(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  /**
   * Convert object to a BSON Document
   *
   * @return BSON Document
   */
  public abstract Document toDocument();
}
