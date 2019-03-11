// TODO: These methods have been lazily taken out of gamestate, by just using gamestate.doAThing, maybe fix that

case class SlackGameState(gameState: GameState,
                          channel: String,
                          thread_ts: Option[String],
                          challenger: Player,
                          defender: Player) {

  // Completely new game
  def this(boardRows: Int,
           boardCols: Int,
           channel: String,
           thread_ts: Option[String],
           challenger: Player,
           defender: Player) {
    this(GameState.newCustomBoard(boardRows, boardCols), channel, thread_ts, challenger, defender)
  }

  // New game with default rows and cols
  def this(channel: String, thread_ts: Option[String], challenger: Player, defender: Player) {
    this(GameState.newDefaultBoard(), channel, thread_ts, challenger, defender)
  }

  def playMove(col: Int, player: Player): Option[SlackGameState] = {

    // Check it's this players turn

    // TODO: Use map here?
    if (gameState.lastMove.isDefined) {
      if (gameState.lastMove.get.player.id == player.id) {
        //TODO: I guess the idea of SlackGameState is that it handles side effects so GameState doesn't have to.
        //But I think it could be possible we could put a lot of this in SlackWrapper. Not a high priority tho.
        SlackWrapper.messageUser("It's not your turn.", player.id, this)
        return None
      }
    }

    // Check col is valid
    if (col < 0 || col > gameState.nBoardCols - 1) {
      SlackWrapper.messageUser("Col is out of bounds.", player.id, this)
      return None
    }

    // Get corresponding column
    val transposedBoard = gameState.board.transpose

    // nBoardRows - 1 is the bottom row of the board, and where we start checking for a valid cell in the column
    val move = gameState.findRow(transposedBoard(col), gameState.nBoardRows - 1, col, player)

    // If column was full
    if (move.row < 0) {
      SlackWrapper.messageUser("Column is full.", player.id, this)
      return None
    }

    // TODO: Should be version of replaceCell which updates the last move as well.
    // TODO: MEGA DANGEROUS, JUST MOCKED CHALLENGER HERE WHILE WORKING ON THINGS
    val newState = gameState.replaceCell(gameState, move.row, move.col, Challenger)

    Some(SlackGameState(
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

    // Done on advice from the fp guild talk on Wed 13th
    gameState.maybeWinningBoard() match {
      case Some(newState) =>
        SlackWrapper.messageUser(newState.boardAsString(), channel, thread_ts, gameState.lastMove.get.player.id)
        Some(gameState.lastMove.get.player)
      case _ => None
    }

  }
}
