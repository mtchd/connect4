package connect4

import connect4.game.{Cell, Challenger, Empty, GameState, Move}
import org.scalatest.FunSuite

class GameStateTests extends FunSuite {

  // TODO: Make this into real test, which checks the boards are equal. But man that needs a monster string...?
  test("connect4.game.GameState.replaceCells") {
    GameState.newStateWithFourCells(Challenger)
  }

  test("connect4.game.GameState.getDiagonal") {
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
  test("connect4.game.GameState.fourInARow") {
    val gameState = GameState.newDefaultBoard()

    // Down and to right replace cells
    val newState = gameState.replace4Cells(0,0, GameState.LowerRight, Challenger)

    // Get diagonal from 0,0, going down and right as well as up and left.
    val diagonal = newState.getDiagonal(0,0,1)

    newState.fourInARow(diagonal, Challenger)
  }

}