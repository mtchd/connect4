package connect4

object CommandHandler {

  def interpret(message: String, authorId: String, gameInstance: Option[GameInstance]): (Option[GameInstance], Option[String]) = {

    // TODO: Divide into when you have game and when you don't
    val (gi, reply) = message match {
      case CommandsRegex.Challenge(_, opponentId, flags) => challenge(gameInstance, opponentId, authorId, flags)
      case CommandsRegex.Accept(_, flags) =>
        // We assume a player is only in one game at once. Discord does not have threading like
        // slack, so we'll need a new alternative to disambiguate what game they are referring to.
        // TODO: Passing the playerRole is better than the ID
        CommandHandler.accept(gameInstance, authorId, flags)
      case CommandsRegex.Drop(col) => drop(col.toInt, gameInstance, authorId)
      case CommandsRegex.Forfeit(_) => forfeit(gameInstance, authorId)
      case CommandsRegex.Reject(_) => reject(gameInstance, authorId)
      case CommandsRegex.Token(_, token, _) => changeToken(gameInstance, token, authorId)
      case CommandsRegex.Help(_) => (gameInstance, Strings.Help)
      // TODO: Update to return None cleanly
      case _ => (gameInstance, "")
    }

    reply match {
      case "" => (gi, None)
      case x => (gi, Some(x))
    }

  }

  // Adds the challenging and defending players to Vector of games in initiation, and acknowledges with message.
  def challenge(gameInstance: Option[GameInstance], defenderId: String, challengerId: String, emoji: String): (Option[GameInstance], String) = {

     if (gameInstance.isDefined) {
       return (gameInstance, Strings.AlreadyGame)
     }

    // Read flag
    val challengerToken = emoji match {
      case CommandsRegex.Emoji(_, token, _) => token
      case _ => Strings.ChallengerToken
    }

    // This could be done in one line, but I've spaced it out here for better readability
    // TODO: Inconsistent way of doing custom tokens
    val pair             = PlayerPair.newPairFromIdsWithChallengerToken(challengerId, defenderId, challengerToken)
    val newGameInstance  = Some(Challenged(pair))
    val reply            = s"Challenging <@$defenderId>...${Strings.NewChallengeHelp}"

    (newGameInstance, reply)

  }

  private def accept(gameInstance: Option[GameInstance], accepterId: String, flags: String): (Option[GameInstance], String) = {

    val token = flags match {
      case CommandsRegex.Token(_, emoji, _) => emoji
      case _ => Strings.DefenderToken
    }

    gameInstance match {
      case Some(instance @ Challenged(playerPair)) if playerPair.defender.id == accepterId => {
        val playing: GameInstance = instance.startPlayingWithDefenderToken(token)
        val reply = Strings.InGameCommands + "\n" + playing.boardAsString
        (Some(playing), reply)
      }
      case _ => (gameInstance, Strings.FailedAcceptOrReject)
    }

  }

  private def drop(col: Int, gameInstance: Option[GameInstance], playerId: String): (Option[GameInstance], String) = {

    val (newGameInstance, reply) = gameInstance match {
      case Some(instance) => playIf(col, instance, playerId)
      case None => return (gameInstance, Strings.FailedDrop)
    }

    checkWin(newGameInstance, reply)

  }

  private def checkWin(gameInstance: GameInstance, currentReply: String): (Option[GameInstance], String) = {
    gameInstance match {
      case Challenged(_) => ()
      case Playing(gameState, _) => gameState.maybeWinningBoard() match {
        case Some(winningGame) => (None, Strings.Win + winningGame.boardAsString())
        case _ => ()
      }
    }
    (Some(gameInstance), currentReply)

  }

  private def forfeit(gameInstance: Option[GameInstance], playerId: String): (Option[GameInstance], String) = {
    gameInstance match {
      case Some(instance @ Playing(_, _)) if instance.instancePlayerPair.isPlayerInPair(playerId) => {
        (None, Strings.Forfeit)
      }
      case _ => (gameInstance, Strings.FailedForfeit)
    }
  }

  // TODO: This is very similar to forfeit, perhaps they can be merged somehow
  private def reject(gameInstance: Option[GameInstance], playerId: String): (Option[GameInstance], String) = {
    gameInstance match {
      case Some(instance @ Challenged(_)) if instance.instancePlayerPair.isPlayerInPair(playerId) => {
        (None, Strings.Reject)
      }
      case _ => (gameInstance, Strings.FailedAcceptOrReject)
    }
  }

  private def changeToken(gameInstance: Option[GameInstance], token: String, playerId: String): (Option[GameInstance], String) = {

    val (maybeRole, definedInstance) = gameInstance match {
      case Some(instance) => (instance.playerRole(playerId), instance)
      case None => return (gameInstance, Strings.NotInGame)
    }

    maybeRole match {
      case Some(role) => (Some(definedInstance.changeToken(role, token)), Strings.tokenChange(token))
      case None => (gameInstance, Strings.NotInGame)
    }

  }

  // TODO: These last 3 helper functions could be better placed to reduce the bloat of this file

  // TODO: Better name for function
  // Plays a turn if the play meets all the rules
  // TODO: Could bring out the gameInstance part and have only gameState in here
  private def playIf(col: Int, gameInstance: GameInstance, playerId: String): (GameInstance, String) = {

    val optionRole = gameInstance.playerRole(playerId)
    val playerRole = optionRole.getOrElse{ return (gameInstance, Strings.FailedDrop) }

    val playing = gameInstance match {
      case Challenged(_) => return (gameInstance, Strings.FailedDrop)
      case playing @ Playing(_,_) => playing
    }

    val gameState = playing.gameState

    // Check it's this players turn
    // TODO: Returning in the middle of a map is not ideal
    gameState.lastMove.map{ lastMove =>
      if (lastMove.playerRole == playerRole) {
        return (playing, Strings.WrongTurn)
      } else {
        lastMove
      }
    }

    val (newState, reply) = play(col, gameState, playerRole)

    val newInstance = Playing(newState, playing.instancePlayerPair)

    (newInstance, replyWithBoard(newInstance, reply))

  }

  private def replyWithBoard(gameInstance: GameInstance, reply: String): String = {
    reply + "\n" + gameInstance.boardAsString
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
