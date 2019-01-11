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

  test("GameState.maybeWinningBoard") {

    // Horizontal
    assert(maybeWinningBoardTest(0,0,0,1).isDefined)
    // Vertical
    assert(maybeWinningBoardTest(0,0,1,0).isDefined)
    // Down and to right diagonal, from top left corner
    assert(maybeWinningBoardTest(0,0,1,1).isDefined)
    // Down and to right diagonal, from middlish
    assert(maybeWinningBoardTest(2,2,1,1).isDefined)
    // Down adn to right diagonal, from middlish, different last move
    assert(maybeWinningBoardTest(2,2,1,1, new Move(3, 3)).isDefined)
    // Up and to right diagonal
    assert(maybeWinningBoardTest(3,0,-1,1).isDefined)

  }

  def maybeWinningBoardTest(startRow: Int, startCol: Int, direction: Int, zeroIfVertical: Int
                           ): Option[GameState] = {
    maybeWinningBoardTest(
      startRow,
      startCol,
      direction,
      zeroIfVertical,
      new Move(new Player(Strings.testChallengerId, Strings.challengerToken), startRow, startCol))
  }

  def maybeWinningBoardTest(startRow: Int, startCol: Int, direction: Int, zeroIfVertical: Int, lastMove: Move
                           ): Option[GameState] = {

    val gameState = new GameState()

    val newState =
      gameState.replaceCells(startRow, startCol, direction,3, zeroIfVertical, Strings.challengerToken)

    println(newState.boardAsString())

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

    // Get diagonal from 0,0, going down and right as well as up and left.
    val diagonal = newState.getDiagonal(0,0,1)

    // What to expect
    val firstHalf = List.fill(3)(new Cell(Strings.emptySpace))
    val secondHalf = List.fill(4)(new Cell(Strings.challengerToken))

    assert(diagonal == (firstHalf ::: secondHalf))
  }

  test("GameState.fourInARow") {
    val gameState = new GameState()

    // Down and to right replace cells
    val newState = gameState.replaceCells(0,0,1,3,1, Strings.challengerToken)

    // Get diagonal from 0,0, going down and right as well as up and left.
    val diagonal = newState.getDiagonal(0,0,1)

    newState.fourInARow(diagonal, Strings.challengerToken, 0)
  }

}