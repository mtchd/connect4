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

}
