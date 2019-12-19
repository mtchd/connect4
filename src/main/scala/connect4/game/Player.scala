package connect4.game

import connect4.Strings

/**
  * Represents player of a game.
  *
  * @param token Character used to represent players token/disc in the board.
  */
case class Player(id: String, token: String)