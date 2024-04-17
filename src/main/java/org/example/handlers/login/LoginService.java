package org.example.handlers.login;

import java.util.HashMap;
import java.util.Map;
import org.example.databases.DynamoDBUtility;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

public class LoginService {

  private final String email;
  private final String password;

  public LoginService(String email, String password) {
    this.email = email;
    this.password = password;
  }

  public boolean authenticateUser() {
    DynamoDBUtility dbu = new DynamoDBUtility("users");

    Map<String, AttributeValue> requestMap = new HashMap<>();
    requestMap.put("email", AttributeValue.builder().s(email).build());
    requestMap.put("password", AttributeValue.builder().s(password).build());

    Map<String, AttributeValue> map = dbu.get(requestMap);

    return map != null && !map.isEmpty();
  }
}
