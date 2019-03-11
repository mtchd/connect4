
object CommandHandler {

  // TODO: Side effect leak here...needs to be some way of handling these ids
  // TODO: In future, we give the ability to customise token
  // Adds the challenging and defending players to list of games in initiation, and acknowledges with message.
  def challenge(challengePairs: List[PlayerPair], defenderId: String, challengerId: String): (List[PlayerPair], String) = {

    // TODO: Maybe a for-comprehension is better here
    // This could be done in one line, but I've spaced it out here for better readability

    val pair = PlayerPair.newPairFromIds(challengerId, defenderId)

    val newChallengePairs = challengePairs :+ pair

    val reply = s"Challenging <@$defenderId>...${Strings.NewChallengeHelp}"

    (newChallengePairs, reply)

  }

  // TODO: Best way to format this?
  def accept(gameInstances: List[GameInstance], challengePairs: List[PlayerPair], accepterId: String):
  (List[GameInstance], List[PlayerPair], String) = {

    // TODO: Fix this, we shouldn't need this var to do this logic
    var foundOne = false
    var newGameInstances = gameInstances
    var newChallengePairs = challengePairs
    var gameStatePrintout =

    // Check the player is part of a pair
    // TODO: Abstract this to function which returns foundOne, newGameInstances and newChallengePairs?
    challengePairs.foreach( pair =>
      // If true, delete pair and make game
      if (pair.defender.id == accepterId) {
        val gameState = GameState.newDefaultBoard()
        val gameInstance = GameInstance(gameState, pair)
        newGameInstances = newGameInstances :+ gameInstance
        newChallengePairs = challengePairs.filterNot( cPair => pair == cPair)
        foundOne = true
      }
    )

    if (foundOne) {
      (newGameInstances, newChallengePairs, Strings.InGameCommands + "\n" + )
    } else {
      // If not, tell player they stupid
      (gameInstances, challengePairs, Strings.FailedAcceptOrReject)
    }

  }

  def drop(col: Int, gameInstances: List[GameInstance], playerId: String): (List[GameInstance], String) = {

    // TODO: Shouldn't need to use var here
    var reply = "Something went wrong."

    // TODO: Should only change one game instance...but has the potential to do many.
    val newGameInstances = gameInstances.map{ gameInstance =>
      val (newGameInstance, sReply) = playIf(col, gameInstance, playerId)
      reply = sReply
      newGameInstance
    }

    (newGameInstances, reply)
  }

  def playIf(col: Int, gameInstance: GameInstance, playerId: String): (GameInstance, String) = {

    val optionRole = gameInstance.has(playerId)
    val role = optionRole.getOrElse{ return (gameInstance, Strings.FailedDrop) }

    val gameState = gameInstance.gameState

    // Check it's this players turn

    // TODO: Use map here?
    if (gameState.lastMove.isDefined) {
      if (gameState.lastMove.get.player.id == player.id) {
        //TODO: I guess the idea of SlackGameState is that it handles side effects so GameState doesn't have to.
        //But I think it could be possible we could put a lot of this in SlackWrapper. Not a high priority tho.
        SlackWrapper.messageUser("It's not your turn.", player.id, this)
        return None
      }
    }

    // Check col is valid
    if (col < 0 || col > gameState.nBoardCols - 1) {
      SlackWrapper.messageUser("Col is out of bounds.", player.id, this)
      return None
    }

    // Get corresponding column
    val transposedBoard = gameState.board.transpose

    // nBoardRows - 1 is the bottom row of the board, and where we start checking for a valid cell in the column
    val move = gameState.findRow(transposedBoard(col), gameState.nBoardRows - 1, col, player)

    // If column was full
    if (move.row < 0) {
      SlackWrapper.messageUser("Column is full.", player.id, this)
      return None
    }

    // TODO: Should be version of replaceCell which updates the last move as well.
    // TODO: MEGA DANGEROUS, JUST MOCKED CHALLENGER HERE WHILE WORKING ON THINGS
    val newState = gameState.replaceCell(gameState, move.row, move.col, Challenger)

    Some(SlackGameState(
      newState.updateLastMoveOnly(Some(move)),
      channel,
      thread_ts,
      challenger,
      defender
    ))
  }


}
