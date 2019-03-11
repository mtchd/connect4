import org.scalatest.FunSuite

class CommandHandlerTests extends FunSuite {

  test("CommandHandler.challenge") {

    // Mock values
    val pairs: List[PlayerPair] = List.empty
    val challengerId = "007"
    val defenderId = "666"
    val testPair = PlayerPair.newPairFromIds(challengerId, defenderId)

    val (newPairs, reply) = CommandHandler.challenge(pairs, defenderId, challengerId)

    assert(reply == s"Challenging <@$defenderId>...${Strings.newChallengeHelp}")
    assert(newPairs.length == 1)
    assert(newPairs.contains(testPair))
  }

}
