
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
    var gameStatePrintout = "Failed to render game."

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
        gameStatePrintout = gameState.boardAsString(pair.defender, pair.challenger)
      }
    )

    if (foundOne) {
      (newGameInstances, newChallengePairs, Strings.InGameCommands + "\n" + gameStatePrintout )
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
      // TODO: This reply is canoodled
      println("Mapping game instances...")
      reply = sReply
      newGameInstance
    }

    println("Reply:" + reply)
    (newGameInstances, reply)
  }

  def forfeit(gameInstances: List[GameInstance], playerId: String): (List[GameInstance], String) = {

    val newGameInstances = gameInstances.filterNot(gameInstance => gameInstance.has(playerId).isDefined)

    if (newGameInstances.length == gameInstances.length) {
      (gameInstances, Strings.FailedForfeit)
    } else {
      (newGameInstances, Strings.Forfeit)
    }

  }

  // TODO: Better name for function
  // Plays a turn if the play meets all the rules
  // TODO: Could bring out the gameInstance part and have only gameState in here
  private def playIf(col: Int, gameInstance: GameInstance, playerId: String): (GameInstance, String) = {

    val optionRole = gameInstance.has(playerId)
    val playerRole = optionRole.getOrElse{ return (gameInstance, Strings.FailedDrop) }

    val gameState = gameInstance.gameState

    println("In playIf")

    // Check it's this players turn
    // TODO: I highly doubt this works due to the return in the map
    gameState.lastMove.map{ lastMove =>
      if (lastMove.playerRole == playerRole) {
        return (gameInstance, Strings.WrongTurn)
      } else {
        lastMove
      }
    }

    // Check col is valid
    if (col < 0 || col > gameState.nBoardCols - 1) {
      return (gameInstance, Strings.OutOfBounds)
    }

    // Get corresponding column
    val transposedBoard = gameState.board.transpose

    // nBoardRows - 1 is the bottom row of the board, and where we start checking for a valid cell in the column
    val move = gameState.findRow(transposedBoard(col), gameState.nBoardRows - 1, col, playerRole)

    // If column was full
    if (move.row < 0) {
      return (gameInstance, Strings.ColFull)
    }

    // TODO: Should be version of replaceCell which updates the last move as well.
    val newState = gameState.replaceCell(gameState, move.row, move.col, playerRole).updateLastMoveOnly(Some(move))

    val newInstance = GameInstance(newState, gameInstance.playerPair)

    // TODO: This could be less verbose
    (newInstance, newState.boardAsString(gameInstance.playerPair.defender, gameInstance.playerPair.challenger))

  }
}
