import org.scalatest.FunSuite

class CommandHandlerTests extends FunSuite {

  test("CommandHandler.challenge") {

    // Mock values
    val instances: List[GameInstance] = List.empty
    val challengerId = "007"
    val defenderId = "666"
    val testPair = PlayerPair.newPairFromIds(challengerId, defenderId)

    val (newInstances, reply) = CommandHandler.challenge(instances, defenderId, challengerId)

    assert(reply == s"Challenging <@$defenderId>...${Strings.NewChallengeHelp}")
    assert(newInstances.length == 1)
    assert(newInstances.contains(testPair))
  }

}
