resource "aws_apigatewayv2_api" "chess-websocket" {
  name                       = "chess-websocket-api"
  protocol_type              = "WEBSOCKET"
  route_selection_expression = "$request.body.action"
}

################################################################################
# map from lambda function name to route key
################################################################################
locals {
  route_keys = {
    disconnect = "$disconnect",
    default    = "$default",
    message    = "message",
    joinGame   = "joinGame"
  }
}
################################################################################
# Integrations
################################################################################
resource "aws_apigatewayv2_integration" "integrations" {
  for_each = var.websocket_lambdas

  api_id           = aws_apigatewayv2_api.chess-websocket.id
  integration_type = "AWS_PROXY"

  description        = "Lambda example"
  integration_method = "POST"
  integration_uri    = aws_lambda_function.websocket_lambda_functions[each.key].invoke_arn

}

resource "aws_apigatewayv2_integration" "connect-integration" {


  api_id           = aws_apigatewayv2_api.chess-websocket.id
  integration_type = "AWS_PROXY"

  description        = "integration between $connect route and connect lambda"
  integration_method = "POST"
  integration_uri    = aws_lambda_function.websocket_connect_lambda.invoke_arn

}

################################################################################
# Routes
################################################################################
resource "aws_apigatewayv2_route" "routes" {
  for_each = var.websocket_lambdas

  api_id    = aws_apigatewayv2_api.chess-websocket.id
  route_key = local.route_keys[each.key]
  target    = "integrations/${aws_apigatewayv2_integration.integrations[each.key].id}"
}

resource "aws_apigatewayv2_route" "connect-route" {
  api_id    = aws_apigatewayv2_api.chess-websocket.id
  route_key = "$connect"
  target    = "integrations/${aws_apigatewayv2_integration.connect-integration.id}"
  request_parameter {
    request_parameter_key = "route.request.querystring.username"
    required              = true
  }
}

resource "aws_apigatewayv2_route_response" "default-response" {
  api_id             = aws_apigatewayv2_api.chess-websocket.id
  route_id           = aws_apigatewayv2_route.routes["default"].id
  route_response_key = "$default"
}

################################################################################
# Stages
################################################################################
resource "aws_apigatewayv2_stage" "dev-stage" {
  api_id        = aws_apigatewayv2_api.chess-websocket.id
  name          = "dev"
  deployment_id = aws_apigatewayv2_deployment.websocket-deployment.id

  default_route_settings {
    throttling_rate_limit  = 2
    throttling_burst_limit = 5
  }
}

################################################################################
# Deployments
################################################################################
resource "aws_apigatewayv2_deployment" "websocket-deployment" {
  api_id = aws_apigatewayv2_api.chess-websocket.id

  triggers = {

    redeployment = sha1(join(",", tolist([
      jsonencode(aws_apigatewayv2_integration.integrations),
      jsonencode(aws_apigatewayv2_integration.connect-integration),
      jsonencode(aws_apigatewayv2_route.routes),
      jsonencode(aws_apigatewayv2_route.connect-route),
    ])))
  }

  lifecycle {
    create_before_destroy = true
  }

  depends_on = [
    aws_apigatewayv2_route.routes,
    aws_apigatewayv2_integration.integrations,
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
  source_arn = "${aws_apigatewayv2_api.chess-websocket.execution_arn}/${var.stage_name}/${local.route_keys[each.key]}"
}

resource "aws_lambda_permission" "connect_lambda_permission" {

  action        = "lambda:InvokeFunction"
  function_name = "connect"
  principal     = "apigateway.amazonaws.com"

  # The /* part allows invocation from any stage, method and resource path
  # within API Gateway.
  source_arn = "${aws_apigatewayv2_api.chess-websocket.execution_arn}/${var.stage_name}/$connect"
}








