import org.scalatest.FunSuite

class GameStateTests2 extends FunSuite {


  // TODO: Make this into real test, which checks the boards are equal. But man that needs a monster string...?
  test("GameState.replaceCells") {
    newStateWithFourCells(Strings.winningToken)
  }

  def newStateWithFourCells(token: String): GameState = {
    val gameState = new GameState()
    val newState =
      gameState.replaceCells(0,0,0,3,1, token)
    println(newState.boardAsString())
    newState
  }

  /*
  test("GameState.maybeWinningBoard") {

    // Horizontal
    assert(maybeWinningBoardTest(0,0,0,1).isDefined)
    // Vertical
    assert(maybeWinningBoardTest(0,0,1,0).isDefined)
    // Down and to right diagonal
    assert(maybeWinningBoardTest(0,0,1,1).isDefined)
    // Up and to right diagonal
    assert(maybeWinningBoardTest(2,0,-1,1).isDefined)

  }
  */

  def maybeWinningBoardTest(startRow: Int, startCol: Int, direction: Int, zeroIfVertical: Int
                           ): Option[GameState] = {

    val gameState = new GameState()

    val newState =
      gameState.replaceCells(startRow, startCol, direction,3, zeroIfVertical, Strings.challengerToken)

    println(newState.boardAsString())

    val lastMove = new Move(new Player(Strings.testChallengerId, Strings.challengerToken), startRow, startCol)

    val newerState = newState.updateLastMoveOnly(Some(lastMove))

    val maybeBoard = newerState.maybeWinningBoard()

    // This line is confusing, should be better written
    println(maybeBoard.getOrElse(return None).boardAsString())

    maybeBoard
  }

  test("GameState.getDiagonal") {
    val gameState = new GameState()

    // Down and to right replace cells
    val newState = gameState.replaceCells(0,0,1,3,1, Strings.challengerToken)

    println(newState.boardAsString())

    // Get diagonal from 0,0, going down and right as well as up and left.
    val diagonal = newState.getDiagonal(0,0,1)

    val firstHalf = List.fill(3)(new Cell(Strings.emptySpace))
    val secondHalf = List.fill(4)(new Cell(Strings.challengerToken))

    assert(diagonal == (firstHalf ::: secondHalf))
  }

}