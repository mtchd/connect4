/**
  * Possible move within the Connect 4 game.
  * @param player Player who made the move.
  * @param row Row the move ended up at.
  * @param col Column the player chose to drop their token into.
  */
case class Move(player: Player, row: Int, col: Int) {

  // Creates test move
  // TODO: Isolate this test definition
  def this(row: Int, col: Int) = this(Player(Strings.TestChallengerId, Strings.ChallengerToken), row, col)

}
