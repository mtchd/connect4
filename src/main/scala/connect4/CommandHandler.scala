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
      case None => (gameInstance, Strings.FailedDrop)
    }

    checkWin(newGameInstance, reply)

  }

  private def mapGameInstanceWithReply(
    gameInstances: Vector[GameInstance],
    noInstanceReply: String)
    (f: GameInstance => (GameInstance, String)): (Vector[GameInstance], String) = {

    // TODO: Shouldn't need to use var here
    var reply = noInstanceReply

    // TODO: Should only change one game instance...but has the potential to do many.
    val playedGameInstances = gameInstances.map { gameInstance =>
      val (newGameInstance, newReply) = f(gameInstance)
      // TODO: This reply is canoodled
      reply = newReply
      newGameInstance
    }
    (playedGameInstances, reply)
  }

  private def checkWin(gameInstance: GameInstance, currentReply: String): (Option[GameInstance], String) = {
    gameInstance match {
      case Playing(gameState, playerPair) => {

      }

      case Challenged(_) => Some((gameInstance, ))
    }

    val maybeWinningGameState = gameInstance.playing.flatMap(_.gameState.maybeWinningBoard())
    val maybeWinningGameInstance = Playing(maybeWinningGameState, gameInstance.)


    val maybeWinningGameInstance = gameInstance match {
      case Some(playing @ Playing(gameState, playerPair)) => {
        val maybeWinningGameState = gameState.maybeWinningBoard()
        playing.copy(gameState = maybeWinningGameState)
        (None, replyWithBoard(game, Strings.Win))
      }
    }
    val maybeWinningGameState = gameInstance.map()

    maybeWinningGame match {
      case Some(game) =>
      case None => (gameInstance, currentReply)
    }

  }

  // TODO: This should only loop through once, and just pop the value out
  // Returns new game instances and maybe a winning game
  def lookForWinningGame(gameInstances: Vector[GameInstance]): (Vector[GameInstance], Option[GameInstance]) = {

    // Just get winning game
    val maybeWinningGame = returnMaybeWinningGame(gameInstances)

    // Just remove winning game
    val newGameInstances = gameInstances.filterNot {
      case Challenged(_) => false
      case Playing(gameInstance, _) => gameInstance.maybeWinningBoard() match {
        case Some(game) => true
        case _ => false
      }
    }

    (newGameInstances, maybeWinningGame)
  }

  // TODO: This is fucked
  def returnMaybeWinningGame(gameInstances: Vector[GameInstance]): Option[GameInstance] = {
    gameInstances.foreach {
      case Challenged(_) => ()
      case Playing(gameState, playerPair) => gameState.maybeWinningBoard() match {
        case Some(winningGame) => return Some(Playing(winningGame, playerPair))
        case _ => ()
      }
    }
    None
  }

  private def forfeit(gameInstances: Vector[GameInstance], playerId: String): (Vector[GameInstance], String) = {

    // Changes Vector
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
  private def reject(gameInstances: Vector[GameInstance], playerId: String): (Vector[GameInstance], String) = {

    val newGameInstances = gameInstances.filterNot {
      case Challenged(playerPair) => playerPair.roleFromPair(playerId).isDefined
      case Playing(_,_) => false
    }

    if (newGameInstances.length == gameInstances.length)
      (gameInstances, Strings.FailedAcceptOrReject)
    else
      (newGameInstances, Strings.Reject)
  }

  private def changeToken(gameInstances: Vector[GameInstance], token: String, playerId: String): (Vector[GameInstance], String) = {

    mapGameInstanceWithReply(gameInstances, Strings.NotInGame) { gameInstance =>

      val playerRole = gameInstance.instancePlayerPair.roleFromPair(playerId)

      val newGameInstance = playerRole match {
        case Some(role) => gameInstance.changeToken(role, token)
        case None => gameInstance
      }

      val reply = playerRole match {
        case Some(_) => Strings.tokenChange(token)
        case None => Strings.NotInGame
      }

      (newGameInstance, reply)
    }
  }

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
