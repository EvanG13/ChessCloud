resource "aws_cloudwatch_log_group" "retention_policy" {
  for_each          = merge(var.websocket_lambdas, var.rest_lambdas)
  name              = "/aws/lambda/${each.key}"
  retention_in_days = 3
}



