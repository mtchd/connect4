import org.scalatest.FunSuite

class BoardTests2 extends FunSuite {


  // TODO: Make this into real test, which checks the boards are equal. But man that needs a monster string...?
  test("GameState.replaceCells") {
    newStateWithFourCells(Strings.winningToken)
  }

  def newStateWithFourCells(token: String): List[List[Cell]] = {
    val board = new Board()
    val newBoard =
      board.replaceCells(0,0,0,3,1, token)
    println(board.boardAsString(newBoard))
    newBoard
  }

  test("GameState.maybeWinningBoard") {

    // Horizontal
    assert(maybeWinningBoardTest(0,0,0,1).isDefined)
    // Vertical
    assert(maybeWinningBoardTest(0,0,1,0).isDefined)
    // Down and to right diagonal
    assert(maybeWinningBoardTest(0,0,1,1).isDefined)
    // Up and to right diagonal
    assert(maybeWinningBoardTest(3,0,-1,1).isDefined)

  }

  def maybeWinningBoardTest(startRow: Int, startCol: Int, direction: Int, zeroIfVertical: Int
                           ): Option[List[List[Cell]]] = {

    val newBoard = newStateWithFourCells(Strings.challengerToken)
    val lastMove = new Move(new Player(Strings.testChallengerId, Strings.challengerToken), startRow, startCol)
    val maybeBoard = gameState.maybeWinningBoard(lastMove, newBoard)
    // This line is confusing, should be better written
    println(gameState.boardAsString(maybeBoard.getOrElse(return None)))
    maybeBoard
  }

  test("GameState.getDiagonal") {

  }

}