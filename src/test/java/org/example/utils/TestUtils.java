package org.example.utils;

import static io.restassured.RestAssured.given;

import java.util.Map;

public final class TestUtils<T> {

  private static final Map<String, String> corsHeaders =
      Map.of(
          "Access-Control-Allow-Origin", "*",
          "Access-Control-Allow-Headers", "*",
          "Access-Control-Allow-Methods", "POST,OPTIONS");

  public T post(Object request, Class<T> tClass, String endpoint, int statusCode) {
    return given()
        .body(request)
        .when()
        .post(endpoint)
        .then()
        .statusCode(statusCode)
        .headers(corsHeaders)
        .extract()
        .as(tClass);
  }

  public String post(Object request, String endpoint, int statusCode) {
    return given()
        .body(request)
        .when()
        .post(endpoint)
        .then()
        .statusCode(statusCode)
        .headers(corsHeaders)
        .extract()
        .asString();
  }
}
