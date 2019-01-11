
/**
  * Possible move within the Connect 4 game.
  * @param player Player who made the move.
  * @param row Row the move ended up at.
  * @param col Column the player chose to drop their token into.
  */
class Move(val player: Player, val row: Int, val col: Int) {

  // Creates test move
  def this(row: Int, col: Int) = this(new Player(Strings.testChallengerId, Strings.challengerToken), row, col)

}
