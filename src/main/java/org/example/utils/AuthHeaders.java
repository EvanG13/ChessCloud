package org.example.utils;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.HashMap;
import java.util.Map;

public class AuthHeaders {
  private AuthHeaders() {}

  public static Map<String, String> getCorsHeaders() {
    Dotenv dotenv = Dotenv.load();
    final String frontendUrl = dotenv.get("FRONTEND_URL");

    Map<String, String> corsHeaders = new HashMap<>();

    corsHeaders.put("Access-Control-Allow-Origin", frontendUrl);
    corsHeaders.put("Access-Control-Allow-Headers", "Content-Type");
    corsHeaders.put("Access-Control-Allow-Methods", "OPTIONS,POST,GET");

    return corsHeaders;
  }
}
