package org.example.utils;

import static io.restassured.RestAssured.given;

import io.restassured.response.Response;
import java.util.Map;

public final class IntegrationTestUtils<T> {

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

  public Response post(String endpoint, Map<String, String> headers, int statusCode) {
    return given()
        .headers(headers)
        .post(endpoint)
        .then()
        .statusCode(statusCode)
        .extract()
        .response();
  }

  public Response get(
      Map<String, String> headers,
      String endpoint,
      Map<String, String> pathParams,
      int statusCode) {
    return given()
        .headers(headers)
        .pathParams(pathParams)
        .when()
        .get(endpoint)
        .then()
        .statusCode(statusCode)
        .headers(corsHeaders)
        .extract()
        .response();
  }

  public Response get(Map<String, String> headers, String endpoint, int statusCode) {
    return given()
        .headers(headers)
        .when()
        .get(endpoint)
        .then()
        .statusCode(statusCode)
        .headers(corsHeaders)
        .extract()
        .response();
  }

  public Response get(
      Map<String, String> headers,
      Map<String, String> queryStrings,
      String endpoint,
      int statusCode) {
    return given()
        .headers(headers)
        .queryParams(queryStrings)
        .when()
        .get(endpoint)
        .then()
        .statusCode(statusCode)
        .headers(corsHeaders)
        .extract()
        .response();
  }

  public Response get(
      Map<String, String> headers,
      String endpoint,
      Map<String, String> pathParams,
      Map<String, String> queryStrings,
      int statusCode) {
    return given()
        .headers(headers)
        .queryParams(queryStrings)
        .pathParams(pathParams)
        .when()
        .get(endpoint)
        .then()
        .statusCode(statusCode)
        .headers(corsHeaders)
        .extract()
        .response();
  }
}
