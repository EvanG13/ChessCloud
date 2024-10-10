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
  default = "java21"
}

/**
 * key = handler
 *
 * Key = lambda function name
 * handler = the handler function that will be invoked for that lambda, package.Class::method
 */
variable "rest_lambdas" {
  type = map(string)
  default = {
    login             = "org.example.handlers.rest.LoginHandler::handleRequest",
    logout            = "org.example.handlers.rest.LogoutHandler::handleRequest",
    register          = "org.example.handlers.rest.RegisterHandler::handleRequest",
    auth              = "org.example.handlers.rest.AuthHandler::handleRequest",
    stats             = "org.example.handlers.rest.StatsHandler::handleRequest",
    gameState         = "org.example.handlers.rest.GetGameStateHandler::handleRequest",
    archivedGame      = "org.example.handlers.rest.getArchivedGame.GetArchivedGameHandler::handleRequest",
    listArchivedGames = "org.example.handlers.rest.getArchivedGame.ListArchivedGamesHandler::handleRequest"
  }
}

/**
   * key = handler
   *
   * Key = lambda function name
   * handler = the handler function that will be invoked for that lambda, package.Class::method
   */
variable "websocket_lambdas" {
  type = map(string)
  default = {
    disconnect = "org.example.handlers.websocket.DisconnectHandler::handleRequest",
    default    = "org.example.handlers.websocket.DefaultHandler::handleRequest",
    message    = "org.example.handlers.websocket.MessageHandler::handleRequest",
    joinGame   = "org.example.handlers.websocket.JoinGameHandler::handleRequest",
    makeMove   = "org.example.handlers.websocket.MakeMoveHandler::handleRequest",
    resign     = "org.example.handlers.websocket.resign.ResignGameHandler::handleRequest"
  }
}

variable "stage_name" {
  default = "dev"
}

