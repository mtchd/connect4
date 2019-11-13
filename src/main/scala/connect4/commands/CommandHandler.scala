package connect4.commands

import connect4.{game, _}
import connect4.game.{CellContents, Challenged, GameInstance, GameState, Playing}

object CommandHandler {

  def challenge(defenderId: String, challengerId: String, emoji: String): (GameInstance, String) = {

    val challengerToken = CommandsRegex.extractEmoji(emoji, Strings.ChallengerToken)
    val newGameInstance = GameInstance.newChallenge(defenderId, challengerId, challengerToken)
    val reply           = s"Challenging <@$defenderId>...${Strings.NewChallengeHelp}"

    (newGameInstance, reply)

  }

  def accept(gameInstance: GameInstance, accepterId: String, emoji: String): (GameInstance, String) = {

    val defenderToken = CommandsRegex.extractEmoji(emoji, Strings.DefenderToken)

    gameInstance match {
      case instance @ Challenged(playerPair) if playerPair.defender.id == accepterId => {
        val playing: GameInstance = instance.startPlayingWithDefenderToken(defenderToken)
        val reply = Strings.InGameCommands + "\n" + playing.boardAsString
        (playing, reply)
      }
      case _ => (gameInstance, Strings.FailedAcceptOrReject)
    }

  }

  def drop(col: Int, gameInstance: GameInstance, playerId: String): (Option[GameInstance], String) = {

    gameInstance match {
      case playing @ Playing(_,_) => {
        val (newPlaying, reply) = playIf(col, playing, playerId)
        checkWin(newPlaying, reply)
      }
      case _ => (Some(gameInstance), Strings.FailedDrop)
    }

  }

  def forfeit(gameInstance: GameInstance, playerId: String): (Option[GameInstance], String) = {
    gameInstance match {
      case instance @ Playing(_, _) if instance.instancePlayerPair.isPlayerInPair(playerId) => {
        (None, Strings.Forfeit)
      }
      case _ => (Some(gameInstance), Strings.FailedForfeit)
    }
  }

  def reject(gameInstance: GameInstance, playerId: String): (Option[GameInstance], String) = {
    gameInstance match {
      case instance @ Challenged(_) if instance.instancePlayerPair.isPlayerInPair(playerId) => {
        (None, Strings.Reject)
      }
      case _ => (Some(gameInstance), Strings.FailedAcceptOrReject)
    }
  }

  def changeToken(gameInstance: GameInstance, message: String, playerId: String): (GameInstance, String) = {

    val token = CommandsRegex.extractEmoji(message, ":poop:")
    val maybeRole = gameInstance.playerRole(playerId)

    maybeRole match {
      case Some(role) => (gameInstance.changeToken(role, token), Strings.tokenChange(token))
      case None => (gameInstance, Strings.NotInGame)
    }

  }

  // TODO: These last few helper functions could be better placed to reduce the bloat of this file

  // TODO: Better name for function
  // Plays a turn if the play meets all the rules
  // TODO: Could bring out the gameInstance part and have only gameState in here
  private def playIf(col: Int, playing: Playing, playerId: String): (Playing, String) = {

    val playerRole = playing.playerRole(playerId) match {
      case Some(role) => role
      case None => return (playing, Strings.FailedDrop)
    }

    // Check it's this players turn
    playing.gameState.lastMove match {
      case Some(lastMove) if lastMove.playerRole == playerRole => return (playing, Strings.WrongTurn)
      case _ => ()
    }

    val (newState, reply) = play(col, playing.gameState, playerRole)

    val newInstance = game.Playing(newState, playing.instancePlayerPair)

    (newInstance, replyWithBoard(newInstance, reply))

  }

  def checkWin(playing: Playing, currentReply: String): (Option[GameInstance], String) = {

    playing.gameState.maybeWinningBoard() match {
      case Some(gameState) => (None, Strings.Win + gameState.boardAsString(playing))
      case _ => (Some(playing), currentReply)
    }

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
