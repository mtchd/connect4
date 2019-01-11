
object Main {

  // Hardcoded crap
  val defaultBoardCols = 6
  val defaultBoardRows = 7

  // TODO: Classes for more things, such as cells, then have rows/cols as List[Cell]
  // TODO: Tests
  def main(args: Array[String]) {

    // Starts slack version of game
    SlackClient.startListening()

  }

}