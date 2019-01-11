import org.scalatest.FunSuite

class GameStateTests extends FunSuite {
  test("GameState.replaceCells") {

    val gameState = new GameState()
    val newBoard = gameState.replaceCells(0,0,0,3,1)
    println(gameState.boardAsString(newBoard))
  }
}