package org.example.utils;

import java.util.HashMap;
import java.util.Map;
import lombok.experimental.UtilityClass;

@UtilityClass
public final class AuthHeaders {

  public static Map<String, String> getCorsHeaders() {
    Map<String, String> corsHeaders = new HashMap<>();

    corsHeaders.put("Access-Control-Allow-Origin", "*");
    corsHeaders.put(
        "Access-Control-Allow-Headers", "Content-Type,X-Amz-Date,Authorization,X-Api-Key");
    corsHeaders.put("Access-Control-Allow-Methods", "POST,OPTIONS");

    return corsHeaders;
  }
}
