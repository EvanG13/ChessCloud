resource "aws_apigatewayv2_api" "chess-websocket" {
  name                       = "chess-websocket-api"
  protocol_type              = "WEBSOCKET"
  route_selection_expression = "$request.body.action"
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

################################################################################
# Routes
################################################################################
resource "aws_apigatewayv2_route" "routes" {
  for_each = var.websocket_lambdas

  api_id    = aws_apigatewayv2_api.chess-websocket.id
  route_key = each.key

  target = "integrations/${aws_apigatewayv2_integration.integrations[each.key].id}"
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
  api_id = aws_apigatewayv2_api.chess-websocket.id

  triggers = {
    #TODO: make this a for each
    redeployment = sha1(join(",", tolist([
      jsonencode(aws_apigatewayv2_integration.integrations),
      jsonencode(aws_apigatewayv2_route.routes),
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
  source_arn = "${aws_apigatewayv2_api.chess-websocket.execution_arn}/*"
}
