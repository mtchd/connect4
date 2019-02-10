import org.scalatest.FunSuite

class GameStateTests extends FunSuite {


  // TODO: Make this into real test, which checks the boards are equal. But man that needs a monster string...?
  test("GameState.replaceCells") {
    newStateWithFourCells(Strings.WinningToken)
  }

  def newStateWithFourCells(token: String): GameState = {
    val gameState = new GameState()
    val newState =
      gameState.replaceCells(0,0,GameState.Horizontal)
    println(newState.boardAsString())
    newState
  }

  test("GameState.maybeWinningBoard") {

    // TODO: Finish getting rid of these magic numbers
    // Horizontal
    assert(maybeWinningBoardTest(0,0,GameState.Horizontal).isDefined)
    // Vertical
    assert(maybeWinningBoardTest(0,0,GameState.Vertical).isDefined)
    // Down and to right diagonal, from top left corner
    assert(maybeWinningBoardTest(0,0,(1,1)).isDefined)
    // Down and to right diagonal, from middlish
    assert(maybeWinningBoardTest(2,2,(1,1)).isDefined)
    // Down adn to right diagonal, from middlish, different last move
    assert(maybeWinningBoardTest(2,2,(1,1), new Move(3, 3)).isDefined)
    // Up and to right diagonal
    assert(maybeWinningBoardTest(3,0,(-1,1)).isDefined)
    // Up and to right diagonal, last move is at top
    assert(maybeWinningBoardTest(6,0,(-1,1), new Move(3, 3)).isDefined)

  }

  def maybeWinningBoardTest(startRow: Int, startCol: Int, direction: (Int, Int),
                           ): Option[GameState] = {
    maybeWinningBoardTest(
      startRow,
      startCol,
      direction,
      new Move(new Player(Strings.testChallengerId, Strings.ChallengerToken), startRow, startCol))
  }

  def maybeWinningBoardTest(startRow: Int, startCol: Int, direction: (Int, Int), lastMove: Move
                           ): Option[GameState] = {

    val gameState = new GameState()

    val newState =
      gameState.replaceCells(startRow, startCol, direction, Strings.ChallengerToken)

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
    val newState = gameState.replaceCells(0,0,GameState.LowerRight, Strings.ChallengerToken)

    // Get diagonal from 0,0, going down and right as well as up and left.
    val diagonal = newState.getDiagonal(0,0,1)

    // What to expect
    val firstHalf = List.fill(3)(new Cell(Strings.EmptySpace))
    val secondHalf = List.fill(4)(new Cell(Strings.ChallengerToken))

    assert(diagonal == (firstHalf ::: secondHalf))
  }

  // TODO: Finish this test
  test("GameState.fourInARow") {
    val gameState = new GameState()

    // Down and to right replace cells
    val newState = gameState.replaceCells(0,0, GameState.LowerRight, Strings.ChallengerToken)

    // Get diagonal from 0,0, going down and right as well as up and left.
    val diagonal = newState.getDiagonal(0,0,1)

    newState.fourInARow(diagonal, Strings.ChallengerToken, 0)
  }

}