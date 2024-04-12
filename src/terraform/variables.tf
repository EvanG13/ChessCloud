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

/**
 * key = handler
 *
 * Key = lambda function name
 * handler = the handler function that will be invoked for that lambda, package.Class::method
 */
variable "lambdas" {
  type = map(string)
  default = {
    login  = "org.example.handlers.login.LoginHandler::handleRequest",
    logout = "org.example.handlers.logout.LogoutHandler::handleRequest",
  }
}