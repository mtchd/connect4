package connect4

/**
  * Represents player of a game.
  * @param token Character used to represent players token/disc in the board.
  */
case class Player(id: String, token: String)

object Player {

  // TODO Using connect4.CellContents for this isn't super great, because it's a wider scope than what we should be accepting.
  def newDefaultPlayer(id: String, role: CellContents): Player = {

    if (role == Defender) {
      Player(id, Strings.DefenderToken)
    } else {
      Player(id, Strings.ChallengerToken)
    }

  }
}
