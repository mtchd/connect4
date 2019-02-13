// Using a sum type allows two players to have the same token but be recognised by the game as different
sealed trait CellContents
case object Defender extends CellContents
case object Challenger extends CellContents
case object Empty extends CellContents
case object Winner extends CellContents

/**
  * Single cell in the connect 4 board.
  * @param contents String representation of a players token/disc, or empty cell.
  */
// TODO: Convert to sum type, which then translates into string stored in player or strings.
case class Cell(contents: CellContents)
