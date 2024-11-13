package org.example.handlers.websocket.makeMove;

import chariot.util.Board;
import com.mongodb.client.model.Updates;
import java.util.Date;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.example.entities.game.Game;
import org.example.entities.game.GameDbService;
import org.example.entities.move.Move;
import org.example.entities.player.Player;
import org.example.enums.ResultReason;
import org.example.exceptions.BadRequest;
import org.example.exceptions.InternalServerError;
import org.example.exceptions.NotFound;
import org.example.exceptions.Unauthorized;
import org.example.handlers.websocket.gameOver.GameOverService;
import org.example.utils.socketMessenger.SocketEmitter;
import org.example.utils.socketMessenger.SocketMessenger;

@AllArgsConstructor
public class MakeMoveService {

  private GameDbService gameDbService;

  private Board board;

  public MakeMoveService() {
    gameDbService = new GameDbService();
  }

  public MakeMoveService(GameDbService gameDbService) {
    this.gameDbService = gameDbService;
  }

  private boolean isMoveLegal(String move) {
    return board.validMoves().stream().map(Board.Move::uci).toList().contains(move);
  }

  public Game loadGame(String gameId, String connectionId, String playerId)
      throws NotFound, Unauthorized, InternalServerError {
    Game game = gameDbService.get(gameId);

    List<Player> players = game.getPlayers();
    Player player1 = players.get(0);
    Player player2 = players.get(1);

    int index = -1;
    if (playerId.equals(player1.getPlayerId())) {
      index = 0;
    } else if (playerId.equals(player2.getPlayerId())) {
      index = 1;
    }

    if (index == -1) {
      throw new Unauthorized("User is not in this Game");
    }

    if (!players.get(index).getConnectionId().equals(connectionId)) {
      throw new InternalServerError("Connection IDs dont match");
    }

    board = Board.fromFEN(game.getGameStateAsFen());
    return game;
  }

  public String[] getPlayerConnectionIds(Game game) {
    String[] connectionIds = new String[2];

    connectionIds[0] = game.getPlayers().get(0).getConnectionId();
    connectionIds[1] = game.getPlayers().get(1).getConnectionId();

    return connectionIds;
  }

  public boolean isPlayersTurn(Game game, String playerId) {
    List<Player> players = game.getPlayers();
    Player player = players.get(0).getPlayerId().equals(playerId) ? players.get(0) : players.get(1);

    return game.getIsWhitesTurn() == player.getIsWhite();
  }

  public Game makeMove(String moveUCI, Game game, Date time) throws BadRequest {
    if (!isMoveLegal(moveUCI)) {
      throw new BadRequest("Illegal Move: " + moveUCI);
    }

    String san = board.toSAN(moveUCI);
    board = board.play(moveUCI);
    Date lastModified = game.getLastModified();

    long t = ((time.getTime() - lastModified.getTime())) / 1000; // convert to seconds from millis

    Move move =
        Move.builder().moveAsUCI(moveUCI).moveAsSan(san).duration((int) Math.max(t, 1)).build();

    List<Player> updatedPlayers = game.getPlayers();
    Player activePlayer;
    boolean isWhiteTurn = game.getIsWhitesTurn();
    if ((updatedPlayers.getFirst().getIsWhite() && isWhiteTurn)
        || (!updatedPlayers.getFirst().getIsWhite() && !isWhiteTurn)) {
      activePlayer = game.getPlayers().getFirst();
    } else {
      activePlayer = game.getPlayers().getLast();
    }
    activePlayer.setRemainingTime(activePlayer.getRemainingTime() - move.getDuration());
    String updatedGameFen = board.toStandardFEN();
    gameDbService.patch(
        game.getId(),
        Updates.combine(
            Updates.set("gameStateAsFen", updatedGameFen),
            Updates.set("isWhitesTurn", !game.getIsWhitesTurn()),
            Updates.set("lastModified", time),
            Updates.set("players", updatedPlayers),
            Updates.push("moveList", move)));

    game.getMoveList().add(move);
    game.setGameStateAsFen(updatedGameFen);
    game.setIsWhitesTurn(!game.getIsWhitesTurn());

    return game;
  }

  public Map<String, Integer> getRemainingTimes(Game game) {
    List<Player> players = game.getPlayers();
    Player whitePlayer;
    Player blackPlayer;
    if (players.getFirst().getIsWhite()) {
      whitePlayer = players.getFirst();
      blackPlayer = players.getLast();
    } else {
      whitePlayer = players.getLast();
      blackPlayer = players.getFirst();
    }
    int whiteTime = whitePlayer.getRemainingTime();
    int blackTime = blackPlayer.getRemainingTime();
    return Map.of("white", whiteTime, "black", blackTime);
  }


  /*
  * if the game arg is in a state of checkmate, this function will call the GameOverService to handle it
  * returns true if it is a checkmate, false otherwise
  * */
  public boolean handleCheckmate(Game game, SocketMessenger messenger) throws InternalServerError {
    Board board = Board.fromFEN(game.getGameStateAsFen());

    Board.GameState gameState = board.gameState();
    if(gameState.equals(Board.GameState.checkmate)){

      //find out whose turn it would be if checkmate hadn't occurred (they are the losing player)
      boolean isWhiteTurn = game.getIsWhitesTurn();

      Player p1 = game.getPlayers().getFirst();
      Player p2 = game.getPlayers().getLast();

      String losingPlayerId;

      if(p1.getIsWhite() == isWhiteTurn)
        losingPlayerId = p1.getPlayerId();
      else
        losingPlayerId = p2.getPlayerId();

      GameOverService gameOverService = new GameOverService(ResultReason.CHECKMATE, game, losingPlayerId, messenger);
      gameOverService.endGame();

      return true; //checkmate detected
    }

    return false; //no checkmate detected

  }

  /*
   * if the game arg is in a state of draw, this function will call the GameOverService to handle it
   * returns true if game is drawn, false otherwise
   * */
  public boolean handleDraw(Game game, SocketMessenger messenger) throws InternalServerError {
    Board board = Board.fromFEN(game.getGameStateAsFen());

    Board.GameState gameState = board.gameState();

    if(gameState.equals(Board.GameState.draw_by_fifty_move_rule) ||
            gameState.equals(Board.GameState.draw_by_threefold_repetition) ||
            gameState.equals(Board.GameState.stalemate)
    ){
      //handle draw

      //find out whose turn it would be if draw hadn't occurred (they are the reporting player)
      boolean isWhiteTurn = game.getIsWhitesTurn();
      Player p1 = game.getPlayers().getFirst();
      Player p2 = game.getPlayers().getLast();
      String losingPlayerId;
      if(p1.getIsWhite() == isWhiteTurn)
        losingPlayerId = p1.getPlayerId();
      else
        losingPlayerId = p2.getPlayerId();

      ResultReason reason;
      if(gameState.equals(Board.GameState.draw_by_fifty_move_rule))
        reason = ResultReason.FIFTY_MOVE_RULE;
      else if(gameState.equals(Board.GameState.draw_by_threefold_repetition))
        reason = ResultReason.REPETITION;
      else{
        reason = ResultReason.STALEMATE;
      }

      GameOverService gameOverService = new GameOverService(reason, game, losingPlayerId, messenger);
      gameOverService.endGame();
      return true;
    }
    return false;

  }
}
