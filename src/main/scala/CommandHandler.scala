object CommandHandler {

  def interpret(message: String, authorId: String, gameInstances: List[GameInstance]): (List[GameInstance], String) = {
    message match {
      // Might not be in commandHandler's scope
      case CommandsRegex.Challenge(_, opponentId, _) => challenge(gameInstances, opponentId, authorId)
      case CommandsRegex.Accept(_, _) =>
        // We assume a player is only in one game at once. Discord does not have threading like
        // slack, so we'll need a new alternative to disambiguate what game they are referring to.

        // Passing side effects to command handler?
        // Could make a ID type known as DiscordId that handles this, makes it less side effecty
        CommandHandler.accept(gameInstances, authorId)
      // TODO: Is using toString okay?
      case CommandsRegex.Drop(_, col, _) => drop(col.toInt, gameInstances, authorId)
      case CommandsRegex.Forfeit(_) => forfeit(gameInstances, authorId)
      case CommandsRegex.Reject(_) => reject(gameInstances, authorId)
      case _ => (gameInstances, Strings.Help)
    }
  }

  // TODO: Side effect leak here...needs to be some way of handling these ids
  // TODO: In future, we give the ability to customise token
  // Adds the challenging and defending players to list of games in initiation, and acknowledges with message.
  private def challenge(gameInstances: List[GameInstance], defenderId: String, challengerId: String): (List[GameInstance], String) = {

    // This could be done in one line, but I've spaced it out here for better readability

    val pair             = PlayerPair.newPairFromIds(challengerId, defenderId)
    val newGameInstance  = Challenged(pair)
    val newGameInstances = gameInstances :+ newGameInstance
    val reply            = s"Challenging <@$defenderId>...${Strings.NewChallengeHelp}"

    (newGameInstances, reply)

  }

  // TODO: Come back to this with Jake, at the moment it goes through the list twice.
  private def accept(gameInstances: List[GameInstance], accepterId: String): (List[GameInstance], String) = {

    val challengeToAccept: Option[GameInstance] =
      gameInstances.find {
        case Challenged(playerPair) => playerPair.defender.id == accepterId
        case Playing(_, _) => false
      }

    val reply = challengeToAccept match {
      case Some(gameInstance) => {
        val playing: Playing = gameInstance.startPlaying
        val gameStatePrintout = playing.boardAsString
        Strings.InGameCommands + "\n" + gameStatePrintout
      }
      case None => Strings.FailedAcceptOrReject
    }

    val newGameInstances = gameInstances.map {
      case challenged @ Challenged(playerPair) if playerPair.defender.id == accepterId => challenged.startPlaying
      case gameInstance => gameInstance
    }

    (newGameInstances, reply)
  }

  private def drop(col: Int, gameInstances: List[GameInstance], playerId: String): (List[GameInstance], String) = {

    // TODO: Shouldn't need to use var here
    var reply = "You're not in a game"

    // TODO: Should only change one game instance...but has the potential to do many.
    val newGameInstances = gameInstances.map{ gameInstance =>
      val (newGameInstance, newReply) = playIf(col, gameInstance, playerId)
      // TODO: This reply is canoodled
      reply = newReply
      newGameInstance
    }

    (newGameInstances, reply)
  }

  private def forfeit(gameInstances: List[GameInstance], playerId: String): (List[GameInstance], String) = {

    // Changes list
    val newGameInstances = gameInstances.filterNot {
      case Challenged(_) => false
      case Playing(_, playerPair) => playerPair.roleFromPair(playerId).isDefined
    }

    // Determines what reply to give
    if (newGameInstances.length == gameInstances.length)
      (gameInstances, Strings.FailedForfeit)
    else
      (newGameInstances, Strings.Forfeit)

  }

  // TODO: This is very similar to forfeit, perhaps they can be merged somehow
  private def reject(gameInstances: List[GameInstance], playerId: String): (List[GameInstance], String) = {

    val newGameInstances = gameInstances.filterNot {
      case Challenged(playerPair) => playerPair.roleFromPair(playerId).isDefined
      case Playing(_,_) => false
    }

    if (newGameInstances.length == gameInstances.length)
      (gameInstances, Strings.FailedAcceptOrReject)
    else
      (newGameInstances, Strings.Reject)
  }

  // TODO: Better name for function
  // Plays a turn if the play meets all the rules
  // TODO: Could bring out the gameInstance part and have only gameState in here
  private def playIf(col: Int, gameInstance: GameInstance, playerId: String): (GameInstance, String) = {

    val optionRole = gameInstance.playerRole(playerId)
    val playerRole = optionRole.getOrElse{ return (gameInstance, Strings.FailedDrop) }
    val playing = gameInstance match {
      case Challenged(_) => { return (gameInstance, Strings.FailedDrop) }
      case playing @ Playing(_,_) => playing
    }

    val gameState = playing.gameState

    // Check it's this players turn
    // TODO: I highly doubt this works due to the return in the map
    gameState.lastMove.map{ lastMove =>
      if (lastMove.playerRole == playerRole) {
        return (playing, Strings.WrongTurn)
      } else {
        lastMove
      }
    }

    val (newState, reply) = play(col, gameState, playerRole)

    val newInstance = Playing(newState, playing.playerPair)

    // TODO: This could be less verbose
    val replyWithBoard = reply + "\n" + newState.boardAsString(playing.playerPair.defender, playing.playerPair.challenger)

    (newInstance, replyWithBoard)

  }

  def play(col: Int, gameState: GameState, playerRole: CellContents): (GameState, String) = {

    // Check col is valid
    if (col < 0 || col > gameState.nBoardCols - 1) {
      return (gameState, Strings.OutOfBounds)
    }

    // Get corresponding column
    val transposedBoard = gameState.board.transpose

    // nBoardRows - 1 is the bottom row of the board, and where we start checking for a valid cell in the column
    val move = gameState.findRow(transposedBoard(col), gameState.nBoardRows - 1, col, playerRole)

    // If column was full
    if (move.row < 0) {
      return (gameState, Strings.ColFull)
    }

    // TODO: Should be version of replaceCell which updates the last move as well.
    val newState = gameState.replaceCell(gameState, move.row, move.col, playerRole).updateLastMoveOnly(Some(move))

    (newState, Strings.dropSuccess(col))

  }


}
