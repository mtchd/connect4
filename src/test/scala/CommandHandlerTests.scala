import org.scalatest.FunSuite

class CommandHandlerTests extends FunSuite {

  val challengerId = "007"
  val defenderId = "666"
  val testPair: PlayerPair = PlayerPair.newPairFromIds(challengerId, defenderId)

  test("CommandHandler.challenge") {

    // Mock values
    val instances: List[GameInstance] = List.empty

    val (newInstances, reply) = CommandHandler.challenge(instances, defenderId, challengerId)

    assert(reply == s"Challenging <@$defenderId>...${Strings.NewChallengeHelp}")
    assert(newInstances.length == 1)
    assert(newInstances.contains(Challenged(testPair)))
  }

  test("CommandHandler.lookForWinningGame should return None if empty") {
    val instances: List[GameInstance] = List.empty
    val (_, maybeWinningGame) = CommandHandler.lookForWinningGame(instances)
    assert(maybeWinningGame.isEmpty)
  }

  test("lookForWinningGame should return game with trophies if winner") {

    val board = GameState.newDefaultBoard()

    val boardWithConnect4 = board.replace4Cells(board.nBoardRows - 1,0, GameState.Horizontal, Defender)

    val boardWithPrevMove = boardWithConnect4.updateLastMoveOnly(Some(Move(Defender, board.nBoardRows - 1, 3)))

    val gameInstance = Playing(boardWithPrevMove, testPair)

    val instances: List[GameInstance] = List(gameInstance)

    val (newInstances, maybeWinningGame) = CommandHandler.lookForWinningGame(instances)

    // TODO: Should actually look for trophies
    assert(maybeWinningGame.isDefined)

  }
}
