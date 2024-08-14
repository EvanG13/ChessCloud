resource "aws_apigatewayv2_api" "chess-websocket" {
  name                       = "chess-websocket-api"
  protocol_type              = "WEBSOCKET"
  route_selection_expression = "$request.body.action"
}

################################################################################
# Integrations
################################################################################
resource "aws_apigatewayv2_integration" "connect-integration" {
  api_id           = aws_apigatewayv2_api.chess-websocket.id
  integration_type = "AWS_PROXY"


  description        = "Lambda example"
  integration_method = "POST"
  integration_uri    = aws_lambda_function.lambda_functions["connect"].invoke_arn
}

resource "aws_apigatewayv2_integration" "disconnect-integration" {
  api_id           = aws_apigatewayv2_api.chess-websocket.id
  integration_type = "AWS_PROXY"


  description        = "Lambda example"
  integration_method = "POST"
  integration_uri    = aws_lambda_function.lambda_functions["disconnect"].invoke_arn
}

################################################################################
# Routes
################################################################################
resource "aws_apigatewayv2_route" "connect-route" {
  api_id    = aws_apigatewayv2_api.chess-websocket.id
  route_key = "$connect"

  target = "integrations/${aws_apigatewayv2_integration.connect-integration.id}"
}

resource "aws_apigatewayv2_route" "disconnect-route" {
  api_id    = aws_apigatewayv2_api.chess-websocket.id
  route_key = "$disconnect"

  target = "integrations/${aws_apigatewayv2_integration.disconnect-integration.id}"
}

################################################################################
# Stages
################################################################################
resource "aws_apigatewayv2_stage" "dev-stage" {
  api_id = aws_apigatewayv2_api.chess-websocket.id
  name   = "dev"
}

################################################################################
# Deployments
################################################################################
resource "aws_apigatewayv2_deployment" "websocket-deployment" {
  api_id      = aws_apigatewayv2_api.chess-websocket.id
  description = "Example deployment"
  triggers = {
    #TODO: make this a for each
    redeployment = sha1(join(",", tolist([
      jsonencode(aws_apigatewayv2_integration.connect-integration),
      jsonencode(aws_apigatewayv2_integration.disconnect-integration),
      jsonencode(aws_apigatewayv2_route.connect-route),
      jsonencode(aws_apigatewayv2_route.disconnect-route),
    ])))
  }
  lifecycle {
    create_before_destroy = true
  }

  depends_on = [
    aws_apigatewayv2_route.connect-route, aws_apigatewayv2_route.disconnect-route,
    aws_apigatewayv2_integration.connect-integration, aws_apigatewayv2_integration.disconnect-integration
  ]
}

################################################################################
# Lambda Permissions
################################################################################
resource "aws_lambda_permission" "lambda_ws_permission" {
  for_each = var.websocket_lambdas

  action        = "lambda:InvokeFunction"
  function_name = each.key
  principal     = "apigateway.amazonaws.com"

  # The /* part allows invocation from any stage, method and resource path
  # within API Gateway.
  source_arn = "${aws_apigatewayv2_api.chess-websocket.execution_arn}/*"
}
