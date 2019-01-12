// TODO: These methods have been lazily taken out of gamestate, by just using gamestate.doAThing, maybe fix that

class SlackGameState(val gameState: GameState,
                     val channel: String,
                     val thread_ts: Option[String],
                     val challenger: Player,
                     val defender: Player) {

  // Completely new game
  def this(boardRows: Int,
           boardCols: Int,
           channel: String,
           thread_ts: Option[String],
           challenger: Player,
           defender: Player) {
    this(new GameState(boardRows, boardCols), channel, thread_ts, challenger, defender)
  }

  // New game with default rows and cols
  def this(channel: String, thread_ts: Option[String], challenger: Player, defender: Player) {
    this(new GameState(), channel, thread_ts, challenger, defender)
  }

  def playMove(col: Int, player: Player): Option[SlackGameState] = {

    // Check it's this players turn

    // TODO: Use map here
    if (gameState.lastMove.isDefined) {
      if (gameState.lastMove.get.player.slackId == player.slackId) {
        //TODO: I guess the idea of SlackGameState is that it handles side effects so GameState doesn't have to.
        // But I think it could be possible we could put a lot of this in SlackClient. Not a high priority tho.
        SlackClient.messageUser("It's not your turn.", player.slackId, this)
        return None
      }
    }

    // Check col is valid
    if (col < 0 || col > gameState.nBoardCols - 1) {
      SlackClient.messageUser("Col is out of bounds.", player.slackId, this)
      return None
    }

    // Get corresponding column
    val transposedBoard = gameState.board.transpose

    // nBoardRows - 1 is the bottom row of the board, and where we start checking for a valid cell in the column
    val move = gameState.findRow(transposedBoard(col), gameState.nBoardRows - 1, col, player)

    // If column was full
    if (move.row < 0) {
      SlackClient.messageUser("Column is full.", player.slackId, this)
      return None
    }

    // TODO: Should be version of replaceCell which updates the last move as well.
    val newState = gameState.replaceCell(gameState, move.row, move.col, player.token)

    Some(new SlackGameState(
      newState.updateLastMoveOnly(Some(move)),
      channel,
      thread_ts,
      challenger,
      defender
    ))
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
