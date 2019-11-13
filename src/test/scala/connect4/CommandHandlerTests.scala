package connect4

import connect4.commands.CommandHandler
import connect4.game.{Challenger, GameInstance, GameState, PlayerPair, Playing}
import org.scalatest.FunSuite

class CommandHandlerTests extends FunSuite {

  val challengerId = "007"
  val defenderId = "666"

  test("CommandHandler.challenge") {

    val (_, reply) = CommandHandler.challenge( defenderId, challengerId, "")

    assert(reply == s"Challenging <@$defenderId>...${Strings.NewChallengeHelp}")

  }

  test("CommandHandler.checkWin if not won") {

    // Mock bois
    val gameInstance = Playing(GameState.newDefaultBoard(), PlayerPair.newTestPair())
    val currentReply = Strings.dropSuccess(1)

    val potentialWin: (Option[GameInstance], String) = CommandHandler.checkWin(gameInstance, currentReply)

    // Will return if they haven't won
    assert(potentialWin._1.isDefined)
  }

  test("CommandHandler.checkWin if have won") {

    val gameState = GameState.newStateWithFourCellsAndLastMove(Challenger)

    assert(gameState.maybeWinningBoard().isDefined)

    // Mock bois
    val gameInstance = Playing(
      gameState,
      PlayerPair.newTestPair()
    )

    val currentReply = Strings.dropSuccess(1)

    val potentialWin: (Option[GameInstance], String) = CommandHandler.checkWin(gameInstance, currentReply)

//  Will return if they haven't won
    assert(potentialWin._1.isEmpty)
  }

}
