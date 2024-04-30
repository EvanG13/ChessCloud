resource "aws_iam_role" "iam_role_for_lambda" {
  name = "lambda_invoke_role"

  assume_role_policy = <<POLICY
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Effect": "Allow",
      "Principal": {
        "Service": "lambda.amazonaws.com"
      },
      "Action": "sts:AssumeRole"
    }
  ]
}
POLICY
}

resource "aws_iam_role_policy_attachment" "invoke_lambda_policy" {
  policy_arn = "arn:aws:iam::aws:policy/service-role/AWSLambdaBasicExecutionRole"
  role       = aws_iam_role.iam_role_for_lambda.name
}

# Lambda function resource
resource "aws_lambda_function" "lambda_functions" {
  for_each = var.lambdas

  function_name = each.key

  s3_bucket = aws_s3_bucket.lambda_bucket.id
  s3_key    = aws_s3_object.project_jar.key

  source_code_hash = filebase64sha256("../../${path.module}/target/chess-cloud-1.0-SNAPSHOT.jar")

  runtime = var.lambda_runtime
  handler = each.value

  memory_size = 1536

  role = aws_iam_role.iam_role_for_lambda.arn
}
