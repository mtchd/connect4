// Using a sum type allows two players to have the same token but be recognised by the game as different
sealed trait CellContents
case object Defender extends CellContents
case object Challenger extends CellContents
case object Empty extends CellContents
case object Winner extends CellContents

case class Cell(contents: CellContents)
