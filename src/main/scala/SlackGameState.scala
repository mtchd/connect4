// TODO: These methods have been lazily taken out of gamestate, by just using gamestate.doAThing, maybe fix that

class SlackGameState(val gameState: GameState,
                     val channel: String,
                     val thread_ts: Option[String],
                     val challenger: Player,
                     val defender: Player,
                     defendersTurn: Boolean) {

  // Completely new game
  def this(boardRows: Int,
           boardCols: Int,
           channel: String,
           thread_ts: Option[String],
           challenger: Player,
           defender: Player,
           defendersTurn: Boolean) {
    this(new GameState(boardRows, boardCols), channel, thread_ts, challenger, defender, defendersTurn)
  }

  // New game with default rows and cols
  def this(channel: String, thread_ts: Option[String], challenger: Player, defender: Player, defendersTurn: Boolean) {
    this(new GameState(), channel, thread_ts, challenger, defender, defendersTurn)
  }

  def playMove(col: Int, playerId: String): Option[SlackGameState] = {

    // Check it's this players turn
    // TODO: Some non-functional stuff here, needs to be changed.

    // The gamestate stores the player who did the last move. So we can check it's not that player.

    // By this stage we already know it's a player playing the game, as we have checked earlier.

    var optionPlayer: Option[Player] = None

    if (!defendersTurn && challenger.slackId == playerId) {
      optionPlayer = Some(challenger)
    }
    else if (defendersTurn && defender.slackId == playerId) {
      optionPlayer = Some(defender)
    }

    val player = optionPlayer.getOrElse {
      SlackClient.messageUser("It's not your turn.", channel, thread_ts, playerId)
      return None
    }

    // Check col is valid
    if (col < 0 || col > gameState.nBoardCols - 1) {
      SlackClient.messageUser("Col is out of bounds.", channel, thread_ts, playerId)
      return None
    }

    // Get corresponding column
    val transposedBoard = gameState.board.transpose

    // nBoardRows - 1 is the bottom row of the board, and where we start checking for a valid cell in the column
    val move = gameState.findRow(transposedBoard(col), gameState.nBoardRows - 1, col, player)

    // If column was full
    if (move.row < 0) {
      SlackClient.messageUser("Column is full.", channel, thread_ts, playerId)
      return None
    }

    val newState = gameState.replaceCell(gameState, move.row, move.col, player.token)

    Some(new SlackGameState(
      newState.updateLastMoveOnly(Some(move)),
      channel,
      thread_ts,
      challenger,
      defender,
      !defendersTurn))
  }

  /**
    * Checks if a player has won.
    * @return Winning player, or None if there is no winner yet.
    */
  def checkWin(): Option[Player] = {

    val newState = gameState.maybeWinningBoard()

    if (newState.isDefined) {
      SlackClient.messageUser(newState.get.boardAsString(), channel, thread_ts, gameState.lastMove.get.player.slackId)
      Some(gameState.lastMove.get.player)
    }
    else {
      None
    }

  }
}
