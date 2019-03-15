import scala.concurrent.Future

object CommandHandler {

  // TODO: Side effect leak here...needs to be some way of handling these ids
  // TODO: In future, we give the ability to customise token
  // Adds the challenging and defending players to list of games in initiation, and acknowledges with message.
  def challenge(gameInstances: List[GameInstance], defenderId: String, challengerId: String): (List[GameInstance], String) = {

    // TODO: Maybe a for-comprehension is better here
    // This could be done in one line, but I've spaced it out here for better readability

    val pair             = PlayerPair.newPairFromIds(challengerId, defenderId)
    val newGameInstance  = Challenged(pair)
    val newGameInstances = gameInstances :+ newGameInstance
    val reply            = s"Challenging <@$defenderId>...${Strings.NewChallengeHelp}"

    (newGameInstances, reply)

  }

  // TODO: Best way to format this?
  def accept(gameInstances: List[GameInstance], accepterId: String):
  (List[GameInstance], String) = {

    val challengeToAccept: Option[GameInstance] =
      gameInstances.find{
        case Challenged(playerPair) => playerPair.defender.id == accepterId
        case Playing(_, _) => false
      }

    val (playing, reply) = challengeToAccept match {
      case Some(gameInstance) => {
        val playing: Playing = gameInstance.startPlaying
        val gameStatePrintout = playing.boardAsString
        (playing, Strings.InGameCommands + "\n" + gameStatePrintout)
      }
      case None => (gameInstances, Strings.FailedAcceptOrReject)
    }

    val newGameInstances = gameInstances.map {
      case challenged @ Challenged(playerPair) if playerPair.defender.id == accepterId => challenged.startPlaying
      case gameInstance => gameInstance
    }

    (newGameInstances, reply)

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

  // TODO: Logic could be nicer
  def forfeit(gameInstances: List[GameInstance], playerId: String): (List[GameInstance], String) = {

    val newGameInstances = gameInstances.filterNot(gameInstance => gameInstance.playerRole(playerId).isDefined)

    if (newGameInstances.length == gameInstances.length) {
      (gameInstances, Strings.FailedForfeit)
    } else {
      (newGameInstances, Strings.Forfeit)
    }

  }

  // TODO: This is very similar to forfeit, perhaps they can be merged somehow
  def reject(gameInstances: List[GameInstance], playerId: String): (List[GameInstance], String) = {

    val newGameInstances = gameInstances.filterNot(gameInstance => gameInstance.pair.defender.id == playerId)

    if (newGameInstances.length == gameInstances.length) {
      (gameInstances, Strings.FailedAcceptOrReject)
    } else {
      (newGameInstances, Strings.Reject)
    }


  }

  // TODO: Better name for function
  // Plays a turn if the play meets all the rules
  // TODO: Could bring out the gameInstance part and have only gameState in here
  private def playIf(col: Int, thing: GameInstance, playerId: String): (GameInstance, String) = {


    val optionRole = thing.playerRole(playerId)
    val playerRole = optionRole.getOrElse{ return (thing, Strings.FailedDrop) }
    val gameInstance = thing match {
      case Challenged(_) => return (thing, Strings.FailedDrop)
      case playing @ Playing(_,_) => playing
    }

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

    val newInstance = Playing(newState, gameInstance.playerPair)

    // TODO: This could be less verbose
    (newInstance, newState.boardAsString(gameInstance.playerPair.defender, gameInstance.playerPair.challenger))

  }
}
