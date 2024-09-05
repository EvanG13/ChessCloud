package org.example.handlers.auth;

import static com.amazonaws.services.lambda.runtime.events.IamPolicyResponse.allowStatement;
import static com.amazonaws.services.lambda.runtime.events.IamPolicyResponse.denyStatement;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2CustomAuthorizerEvent;
import com.amazonaws.services.lambda.runtime.events.IamPolicyResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.example.requestRecords.AuthRequest;
import org.example.utils.DotenvClass;

public class AuthHandler
    implements RequestHandler<APIGatewayV2CustomAuthorizerEvent, IamPolicyResponse> {

  private final AuthService service;

  public AuthHandler(AuthService service) {
    this.service = service;
  }

  public AuthHandler() {
    service = new AuthService();
  }

  @Override
  public IamPolicyResponse handleRequest(APIGatewayV2CustomAuthorizerEvent event, Context context) {
    Map<String, String> headers = event.getHeaders();
    APIGatewayV2CustomAuthorizerEvent.RequestContext requestContext = event.getRequestContext();

    String token = headers.get("Authorization").replace("Bearer ", "").replace("\"", "");

    IamPolicyResponse.PolicyDocument policyDocument = new IamPolicyResponse.PolicyDocument();
    policyDocument.setVersion("2012-10-17");

    String userId = headers.get("userId").replace("userId ", "").replace("\"", "");

    StringBuilder resource =
        new StringBuilder("arn:aws:execute-api")
            .append(":")
            .append(DotenvClass.dotenv.get("AWS_REGION"))
            .append(":")
            .append(requestContext.getAccountId())
            .append(":")
            .append(requestContext.getApiId())
            .append("/*/*/*");

    List<IamPolicyResponse.Statement> statements = new ArrayList<>();

    if (service.isValidSession(new AuthRequest(token, userId))) {
      statements.add(allowStatement(resource.toString()));
    } else {
      statements.add(denyStatement(resource.toString()));
    }

    policyDocument.setStatement(statements);
    return IamPolicyResponse.builder()
        .withPolicyDocument(policyDocument)
        .withPrincipalId(userId)
        .build();
  }
}
