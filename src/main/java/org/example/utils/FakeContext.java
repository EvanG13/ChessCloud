package org.example.utils;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import lombok.Getter;

@Getter
public class FakeContext implements Context {

  @Override
  public String getAwsRequestId() {
    return "fake-request-id";
  }

  @Override
  public String getLogGroupName() {
    return "test-log-group";
  }

  @Override
  public String getLogStreamName() {
    return "test-log-stream";
  }

  @Override
  public String getFunctionName() {
    return "";
  }

  @Override
  public String getFunctionVersion() {
    return "";
  }

  @Override
  public String getInvokedFunctionArn() {
    return "";
  }

  @Override
  public CognitoIdentity getIdentity() {
    return null;
  }

  @Override
  public ClientContext getClientContext() {
    return null;
  }

  @Override
  public int getRemainingTimeInMillis() {
    return 0;
  }

  @Override
  public int getMemoryLimitInMB() {
    return 0;
  }

  @Override
  public LambdaLogger getLogger() {
    return null;
  }
}
