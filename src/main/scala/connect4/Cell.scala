package connect4

// Using a sum type allows two players to have the same token but be recognised by the game as different
sealed trait CellContents
case object Defender extends CellContents
case object Challenger extends CellContents
case object Empty extends CellContents
case object Winner extends CellContents

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
