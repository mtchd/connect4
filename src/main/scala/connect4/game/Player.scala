package connect4.game

import connect4.Strings

/**
  * Represents player of a game.
  *
  * @param token Character used to represent players token/disc in the board.
  */
case class Player(id: String, token: String)

object Player {

  // TODO Using connect4.game.CellContents for this isn't super great, because it's a wider scope than what we should be accepting.
  def newDefaultPlayer(id: String, role: CellContents): Player = {

    if (role == Defender) {
      Player(id, Strings.DefaultDefenderToken)
    } else {
      Player(id, Strings.DefaultChallengerToken)
    }

  }
}
