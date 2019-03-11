/**
  * Represents player of a game.
  * @param token Character used to represent players token/disc in the board.
  */
// TODO: Rmove slack id from player
case class Player(id: String, token: String, role: CellContents) {

}

object Player {

  def defaultTokenPlayer(id: String, role: CellContents): Unit = {

    if (role == Defender) {
      Player(id, Strings.DefenderToken)
    }
    Player(id, Strings. )
  }
}
