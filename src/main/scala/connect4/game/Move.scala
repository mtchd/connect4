package connect4.game

/**
  * Possible move within the Connect 4 game.
  *
  * @param playerRole Role of player who made the move.
  * @param row Row the move ended up at.
  * @param col Column the player chose to drop their token into.
  */
case class Move(playerRole: CellContents, row: Int, col: Int)
