package connect4

import org.scalatest.FunSuite

class GameStateTests extends FunSuite {

  // TODO: Make this into real test, which checks the boards are equal. But man that needs a monster string...?
  test("connect4.GameState.replaceCells") {
    newStateWithFourCells(Challenger)
  }

  def newStateWithFourCells(token: CellContents): GameState = {
    val gameState = GameState.newDefaultBoard()
    val newState =
      gameState.replace4Cells(0,0, GameState.Horizontal, token)
    println(newState.boardAsString())
    newState
  }

  test("connect4.GameState.maybeWinningBoard") {

    // TODO: Finish getting rid of these magic numbers
    // TODO: This only checks if it detects a win, not if it replaces the tokens with winning tokens properly.
    // Horizontal
    assert(maybeWinningBoardTest(0,0, GameState.Horizontal).isDefined)
    // Vertical
    assert(maybeWinningBoardTest(0,0, GameState.Vertical).isDefined)
    // Vertical again, all on left side
    assert(maybeWinningBoardTest(3,0, GameState.Vertical).isDefined)
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
    // Should not be a winning game (Because the move is placed wrong, although there is actually 4 in a row)
    assert(maybeWinningBoardTest(0,0, GameState.Horizontal, new Move(6,5)).isEmpty)

  }

  def maybeWinningBoardTest(startRow: Int, startCol: Int, direction: (Int, Int),
                           ): Option[GameState] = {
    maybeWinningBoardTest(
      startRow,
      startCol,
      direction,
      Move(Challenger, startRow, startCol))
  }

  def maybeWinningBoardTest(startRow: Int, startCol: Int, direction: (Int, Int), lastMove: Move
                           ): Option[GameState] = {

    val gameState = GameState.newDefaultBoard()

    val newState =
      gameState.replace4Cells(startRow, startCol, direction, Challenger)

    // println(newState.boardAsString())

    val newerState = newState.updateLastMoveOnly(Some(lastMove))

    val maybeBoard = newerState.maybeWinningBoard()

    // This line is confusing, should be better written
    println(maybeBoard.getOrElse(return None).boardAsString())

    maybeBoard
  }

  test("connect4.GameState.getDiagonal") {
    val gameState = GameState.newDefaultBoard()

    // Down and to right replace cells
    val newState = gameState.replace4Cells(0,0,GameState.LowerRight, Challenger)

    // Get diagonal from 0,0, going down and right as well as up and left.
    val diagonal = newState.getDiagonal(0,0,1)

    // What to expect
    val firstHalf = Vector.fill(3)(Cell(Empty))
    val secondHalf = Vector.fill(4)(Cell(Challenger))

    assert(diagonal == (firstHalf ++ secondHalf))
  }

  // TODO: Finish this test
  test("connect4.GameState.fourInARow") {
    val gameState = GameState.newDefaultBoard()

    // Down and to right replace cells
    val newState = gameState.replace4Cells(0,0, GameState.LowerRight, Challenger)

    // Get diagonal from 0,0, going down and right as well as up and left.
    val diagonal = newState.getDiagonal(0,0,1)

    newState.fourInARow(diagonal, Challenger)
  }

}