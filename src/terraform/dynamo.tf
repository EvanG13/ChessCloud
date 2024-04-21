resource "aws_dynamodb_table" "user_table" {
  name           = "users"
  billing_mode   = "PROVISIONED"
  read_capacity  = 25
  write_capacity = 25
  hash_key       = "id"

  attribute {
    name = "id"
    type = "S"
  }

  attribute {
    name = "email"
    type = "S"
  }

  global_secondary_index {
    name            = "emailPasswordIndex"
    hash_key        = "email"
    write_capacity  = 25
    read_capacity   = 25
    projection_type = "ALL"
  }

  tags = {
    Name        = "user_table"
    Environment = "production"
  }
}

resource "aws_dynamodb_table_item" "default_user" {
  table_name = aws_dynamodb_table.user_table.name
  hash_key   = aws_dynamodb_table.user_table.hash_key

  item = <<ITEM
{
  "id": {"S": "it-test_user_id"},
  "email": {"S": "it-test@gmail.com"},
  "password": {"S": "$2a$12$MwPTs6UFjy7NAge3HxHwEOTUvX2M6bXpqkHCozjisNTCpcaQ9ZiyC"},
  "username": {"S": "TestUsername"}
}
ITEM
}


