package org.example.entities;

import com.google.gson.annotations.Expose;
import io.github.cdimascio.dotenv.Dotenv;
import java.util.HashMap;
import org.bson.Document;

public abstract class DataTransferObject {

  @Expose protected String id;

  protected HashMap<String, String> corsHeaders;

  public DataTransferObject(String id) {
    Dotenv dotenv = Dotenv.load();
    final String frontendUrl = dotenv.get("FRONTEND_URL");
    this.id = id;
    this.corsHeaders = new HashMap<>();
    this.corsHeaders.put("Access-Control-Allow-Origin", frontendUrl);
    this.corsHeaders.put("Access-Control-Allow-Headers", "Content-Type");
    this.corsHeaders.put("Access-Control-Allow-Methods", "OPTIONS,POST,GET");
  }

  public String getId() {
    return this.id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public HashMap<String, String> getCorsHeaders() {
    return this.corsHeaders;
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
