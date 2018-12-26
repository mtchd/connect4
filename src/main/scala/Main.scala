
object Main {

  // Hardcoded crap
  val nBoardCols = 6
  val nBoardRows = 7

  val playerX = new Player('❌')
  val playerO = new Player('⭕')

  val emptySpace = "⚪"
  val emptySpaceC = '⚪'

  // TODO: Classes for more things, such as cells, then have rows/cols as List[Cell]
  // TODO: Tests
  def main(args: Array[String]) {

    // Starts slack version of game
    SlackClient.startListening()

    // Starts terminal version of game
    ConsoleClient.gameLoop(new GameState(nBoardRows, nBoardCols))

  }

}