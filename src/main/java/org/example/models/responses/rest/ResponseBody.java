package org.example.models.responses.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.example.annotations.CustomExclusionPolicy;

public abstract class ResponseBody {
  public String toJSON() {
    Gson gson = new GsonBuilder().setExclusionStrategies(new CustomExclusionPolicy()).create();
    return gson.toJson(this);
  }
}
