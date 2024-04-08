variable "region" {
  default = "us-east-1"
}

variable "shared_config_files" {
  default = ["~/.aws/config"]
}

variable "shared_credentials_files" {
  default = ["~/.aws/credentials"]
}

variable "profile" {
  default = "default"
}

variable "lambda_runtime" {
  default = "java17"
}

# function_name = handler_location
variable "lambdas" {
  type = map(string)
  default = {
    login = "org.example.handlers.LoginHandler::handleRequest"
  }
}