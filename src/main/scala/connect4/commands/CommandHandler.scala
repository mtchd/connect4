package connect4.commands

import connect4.{game, _}
import connect4.game.{CellContents, Challenged, GameInstance, GameState, Playing}
import connect4.wrappers.Emoji

object CommandHandler {

  def challenge(defenderId: String, challengerId: String, emoji: String, emojis: Vector[Emoji]): (GameInstance, String) = {

    val challengerToken = CommandsRegex.extractEmoji(emoji, Strings.ChallengerToken)

    // If emoji is invalid, we continue with default emoji
    val validatedToken = validateEmoji(emojis, challengerToken, Strings.ChallengerToken)

    val newGameInstance = GameInstance.newChallenge(defenderId, challengerId, validatedToken)
    val reply           = s"Challenging <@$defenderId>...${Strings.NewChallengeHelp}"

    (newGameInstance, reply)

  }

  def accept(gameInstance: GameInstance, accepterId: String, emoji: String, emojis: Vector[Emoji]): (GameInstance, String) = {

    val defenderToken = CommandsRegex.extractEmoji(emoji, Strings.DefenderToken)
    val validatedToken = validateEmoji(emojis, defenderToken, Strings.ChallengerToken)

    gameInstance match {
      case instance @ Challenged(playerPair) if playerPair.defender.id == accepterId => {
        val playing: GameInstance = instance.startPlayingWithDefenderToken(validatedToken)
        val reply = Strings.InGameCommands + "\n" + playing.boardAsString
        (playing, reply)
      }
      case Challenged(playerPair) => (gameInstance, Strings.FailedAcceptOrReject(playerPair.defender.id))
      case _ => (gameInstance, Strings.FailedAcceptOrReject)
    }

  }

  def drop(col: String, gameInstance: GameInstance, playerId: String): (GameInstance, String) = {

    val intCol = col.toInt

    gameInstance match {
      case playing @ Playing(_,_) => playIf(intCol, playing, playerId)
      case _ => (gameInstance, Strings.FailedDrop)
    }

  }

  def forfeit(gameInstance: GameInstance, playerId: String): (GameInstance, String) = {

    gameInstance match {
      case playing @ Playing(_, playerPair) =>  playerPair.roleFromPair(playerId) match {
        case Some(role) => (playing.finishGame(role.opposite), Strings.Forfeit)
        case None => (gameInstance, Strings.FailedForfeit)
      }
      case _ => (gameInstance, Strings.FailedForfeit)
    }

  }

  def reject(gameInstance: GameInstance, playerId: String): (GameInstance, String) = {

    gameInstance match {
      case challenged @ Challenged(playerPair) => playerPair.roleFromPair(playerId) match {
        case Some(_) => (challenged.finishGame, Strings.Reject)
        case None => (gameInstance, Strings.FailedAcceptOrReject(challenged.instancePlayerPair.defender.id))
      }
      case _ => (gameInstance, Strings.FailedAcceptOrReject)
    }

  }

  def changeToken(gameInstance: GameInstance, message: String, playerId: String, emojis: Vector[Emoji]): (GameInstance, String) = {

    val token = CommandsRegex.extractEmoji(message, Strings.FailedToken)
    val validatedToken = validateEmoji(emojis, token, Strings.FailedToken)
    val maybeRole = gameInstance.playerRole(playerId)

    maybeRole match {
      case Some(role) => (gameInstance.changeToken(role, validatedToken), Strings.tokenChange(validatedToken))
      case None => (gameInstance, Strings.NotInGame)
    }

  }

  // TODO: These last few helper functions could be better placed to reduce the bloat of this file

  // TODO: Better name for function
  // Plays a turn if the play meets all the rules
  // TODO: Could bring out the gameInstance part and have only gameState in here
  private def playIf(col: Int, playing: Playing, playerId: String): (GameInstance, String) = {

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

    val newPlaying = game.Playing(newState, playing.instancePlayerPair)

    newPlaying.gameState.maybeWinningBoard() match {
      case Some(gameState) => (playing.finishGame(playerRole), Strings.Win + gameState.boardAsString(playing.instancePlayerPair))
      case _ => (newPlaying, reply + "\n" + newPlaying.boardAsString)
    }

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

  def validateEmoji(emojis: Vector[Emoji], emoji: String, default: String): String = {

    // Strip out the :
    val strippedEmoji = emoji.replaceAll(":","")

    if (emojis.contains(Emoji(strippedEmoji))) emoji else default
  }

}
