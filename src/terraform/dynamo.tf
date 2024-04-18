resource "aws_dynamodb_table" "user_table" {
  name           = "users"
  billing_mode   = "PROVISIONED"
  read_capacity  = 20
  write_capacity = 20
  hash_key       = "id"

  attribute {
    name = "id"
    type = "S"
  }

  attribute {
    name = "email"
    type = "S"
  }

  attribute {
    name = "password"
    type = "S"
  }

  global_secondary_index {
    name               = "emailPasswordIndex"
    hash_key           = "email"
    range_key          = "password"
    write_capacity     = 10
    read_capacity      = 10
    projection_type    = "INCLUDE"
    non_key_attributes = ["id"]
  }

  tags = {
    Name        = "user_table"
    Environment = "production"
  }
}


