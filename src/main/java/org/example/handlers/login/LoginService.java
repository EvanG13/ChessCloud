package org.example.handlers.login;

import org.example.databases.DynamoDBUtility;
import org.example.entities.DynamoUser;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;

public class LoginService {

  private final String email;
  private final String password;

  public LoginService(String email, String password) {
    this.email = email;
    this.password = password;
  }

    public boolean authenticateUser(){
        DynamoDBUtility dbu = new DynamoDBUtility("users");
        Map<String, AttributeValue> map = dbu.get(this.email, this.password);

        if(map==null || map.isEmpty()){
            return false;
        }
        return true;
    }

    public String getResponseMessage() {
        return "New Input email : " + email + "\nNew Input Password : " + password;
    }
}
