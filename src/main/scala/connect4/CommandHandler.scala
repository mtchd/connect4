package connect4

object CommandHandler {

  // Adds the challenging and defending players to Vector of games in initiation, and acknowledges with message.
  def challenge(defenderId: String, challengerId: String, emoji: String): (GameInstance, String) = {

    // Read flag
    val challengerToken = emoji match {
      case CommandsRegex.Emoji(_, token, _) => token
      case _ => Strings.ChallengerToken
    }

    // This could be done in one line, but I've spaced it out here for better readability
    // TODO: Inconsistent way of doing custom tokens
    val pair             = PlayerPair.newPairFromIdsWithChallengerToken(challengerId, defenderId, challengerToken)
    val newGameInstance  = Challenged(pair)
    val reply            = s"Challenging <@$defenderId>...${Strings.NewChallengeHelp}"

    (newGameInstance, reply)

  }

  def accept(gameInstance: GameInstance, accepterId: String, flags: String): (GameInstance, String) = {

    val token = flags match {
      case CommandsRegex.Token(_, emoji, _) => emoji
      case _ => Strings.DefenderToken
    }

    gameInstance match {
      case instance @ Challenged(playerPair) if playerPair.defender.id == accepterId => {
        val playing: GameInstance = instance.startPlayingWithDefenderToken(token)
        val reply = Strings.InGameCommands + "\n" + playing.boardAsString
        (playing, reply)
      }
      case _ => (gameInstance, Strings.FailedAcceptOrReject)
    }

  }

  def drop(col: Int, gameInstance: GameInstance, playerId: String): (Option[GameInstance], String) = {

    val (newGameInstance, reply) = playIf(col, gameInstance, playerId)
    checkWin(newGameInstance, reply)

  }

  def checkWin(gameInstance: GameInstance, currentReply: String): (Option[GameInstance], String) = {

    gameInstance match {
      case Challenged(_) => ()
      case Playing(gameState, _) => gameState.maybeWinningBoard() match {
        case Some(winningGame) => (None, Strings.Win + winningGame.boardAsString())
        case _ => ()
      }
    }
    (Some(gameInstance), currentReply)

  }

  def forfeit(gameInstance: GameInstance, playerId: String): (Option[GameInstance], String) = {

    gameInstance match {
      case instance @ Playing(_, _) if instance.instancePlayerPair.isPlayerInPair(playerId) => {
        (None, Strings.Forfeit)
      }
      case _ => (Some(gameInstance), Strings.FailedForfeit)
    }

  }

  // TODO: This is very similar to forfeit, perhaps they can be merged somehow
  def reject(gameInstance: GameInstance, playerId: String): (Option[GameInstance], String) = {
    gameInstance match {
      case instance @ Challenged(_) if instance.instancePlayerPair.isPlayerInPair(playerId) => {
        (None, Strings.Reject)
      }
      case _ => (Some(gameInstance), Strings.FailedAcceptOrReject)
    }
  }

  def changeToken(gameInstance: GameInstance, token: String, playerId: String): (GameInstance, String) = {

    val maybeRole = gameInstance.playerRole(playerId)

    maybeRole match {
      case Some(role) => (gameInstance.changeToken(role, token), Strings.tokenChange(token))
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
