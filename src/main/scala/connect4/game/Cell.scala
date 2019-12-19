package connect4.game

// Using a sum type allows two players to have the same token but be recognised by the game as different
sealed trait CellContents
case object Empty extends CellContents
case object Winner extends CellContents

sealed trait PlayerRole extends CellContents {
  def opposite: PlayerRole = {
    this match {
      case Defender => Challenger
      case Challenger => Defender
    }
  }
}
case object Defender extends PlayerRole
case object Challenger extends PlayerRole

case class Cell(contents: CellContents)

object Cell {

  def convertToString(cellContents: CellContents): String = {
    cellContents match {
      case Defender => "Defender"
      case Challenger => "Challenger"
      case Empty => "Empty"
      case Winner => "Winner"
    }
  }

  // TODO: Program poops if we get a string that's not one of these
  def convertFromString(string: String): CellContents = {
    string match {
      case "Defender" => Defender
      case "Challenger" => Challenger
      case "Empty" => Empty
      case "Winner" => Winner
    }
  }

}
