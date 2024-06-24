resource "aws_api_gateway_rest_api" "rest_api" {
  name = "chess-rest-api"

  body = file("${path.module}/../openapi/openapi.yaml")
}

resource "aws_api_gateway_deployment" "dev_deployment" {
  rest_api_id = aws_api_gateway_rest_api.rest_api.id

  triggers = {
    redeployment = sha1(yamlencode(aws_api_gateway_rest_api.rest_api.body))
  }

  lifecycle {
    create_before_destroy = true
  }

  depends_on = [
    aws_api_gateway_rest_api.rest_api,
  ]
}

resource "aws_api_gateway_stage" "dev" {
  deployment_id = aws_api_gateway_deployment.dev_deployment.id
  rest_api_id   = aws_api_gateway_rest_api.rest_api.id
  stage_name    = "dev"
}

resource "aws_lambda_permission" "lambda_permission" {
  for_each = var.lambdas

  action        = "lambda:InvokeFunction"
  function_name = each.key
  principal     = "apigateway.amazonaws.com"

  # The /* part allows invocation from any stage, method and resource path
  # within API Gateway.
  source_arn = "${aws_api_gateway_rest_api.rest_api.execution_arn}/*"
}

resource "aws_api_gateway_usage_plan" "usage_plan" {
  name = "usage-plan"

  api_stages {
    api_id = aws_api_gateway_rest_api.rest_api.id
    stage  = aws_api_gateway_stage.dev.stage_name
  }

  throttle_settings {
    burst_limit = 5
    rate_limit  = 5
  }
}

