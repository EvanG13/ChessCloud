package org.example.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

public class TestUtils {

  public static void assertCorsHeaders(Map<String, String> headers) {
    assertEquals(
        "*", headers.get("Access-Control-Allow-Origin"), "Incorrect CORS header for Allow-Origin");
    assertEquals(
        "POST,OPTIONS",
        headers.get("Access-Control-Allow-Methods"),
        "Incorrect CORS header for Allow-Methods");
    assertEquals(
        "*",
        headers.get("Access-Control-Allow-Headers"),
        "Incorrect CORS header for Allow-Headers");
  }
}
