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
    login         = "org.example.handlers.rest.login.LoginHandler::handleRequest",
    logout        = "org.example.handlers.rest.logout.LogoutHandler::handleRequest",
    register      = "org.example.handlers.rest.register.RegisterHandler::handleRequest",
    auth          = "org.example.handlers.rest.auth.AuthHandler::handleRequest",
    stats         = "org.example.handlers.rest.stats.StatsHandler::handleRequest",
    gameState     = "org.example.handlers.rest.getGameState.GetGameStateHandler::handleRequest",
    archivedGame  = "org.example.handlers.rest.getArchivedGame.GetArchivedGameHandler::handleRequest",
    archivedGames = "org.example.handlers.rest.getArchivedGame.ListArchivedGamesHandler::handleRequest"
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
    disconnect = "org.example.handlers.websocket.disconnect.DisconnectHandler::handleRequest",
    default    = "org.example.handlers.websocket.defaultRoute.DefaultHandler::handleRequest",
    message    = "org.example.handlers.websocket.message.MessageHandler::handleRequest",
    joinGame   = "org.example.handlers.websocket.joinGame.JoinGameHandler::handleRequest",
    makeMove   = "org.example.handlers.websocket.makeMove.MakeMoveHandler::handleRequest",
    resign     = "org.example.handlers.websocket.resign.ResignGameHandler::handleRequest",
    offerDraw  = "org.example.handlers.websocket.offerDraw.OfferDrawHandler::handleRequest"
  }
}

variable "stage_name" {
  default = "dev"
}

