package org.example.models.responses;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public abstract class ResponseBody {
  public String toJSON() {
    Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    return gson.toJson(this);
  }
}
