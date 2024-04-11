resource "random_pet" "lambda_bucket_name" {
  prefix = "lambda"
  length = 2
}

resource "aws_s3_bucket" "lambda_bucket" {
  bucket        = random_pet.lambda_bucket_name.id
  force_destroy = true
}

resource "aws_s3_bucket_public_access_block" "lambda_bucket" {
  bucket = aws_s3_bucket.lambda_bucket.id

  block_public_acls       = true
  block_public_policy     = true
  ignore_public_acls      = true
  restrict_public_buckets = true
}

resource "aws_s3_object" "project_jar" {
  bucket = aws_s3_bucket.lambda_bucket.id

  content_type = "jar"

  key    = "chess.jar"
  source = "../../${path.module}/target/chess-cloud-1.0-SNAPSHOT.jar"

  etag = filemd5("../../${path.module}/target/chess-cloud-1.0-SNAPSHOT.jar")
}