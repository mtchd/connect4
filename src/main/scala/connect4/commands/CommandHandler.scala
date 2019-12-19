package connect4.commands

import connect4.{game, _}
import connect4.game.{CellContents, Challenged, Defender, GameInstance, GameState, Playing}
import connect4.wrappers.{Emoji, EmojiHandler}

object CommandHandler {

  def challenge(defenderId: String, challengerId: String, emoji: String, emojiHandler: EmojiHandler): (GameInstance, String) = {

    // If emoji is invalid, we continue with default emoji
    val validatedToken = emojiHandler.validateAndExtractEmoji(emoji, Strings.DefaultChallengerToken)

    val newGameInstance = GameInstance.newChallenge(defenderId, challengerId, validatedToken)
    val reply           = s"Challenging <@$defenderId>...${Strings.NewChallengeHelp}"

    (newGameInstance, reply)

  }

  def accept(gameInstance: GameInstance, accepterRole: CellContents, emoji: String, emojiHandler: EmojiHandler): (GameInstance, String) = {

    val validatedToken = emojiHandler.validateAndExtractEmoji(emoji, Strings.DefaultDefenderToken)

    gameInstance match {
      case challenged @ Challenged(_) if Defender == accepterRole =>
        val playing: GameInstance = challenged.startPlayingWithDefenderToken(validatedToken)
        val reply = Strings.InGameCommands + "\n" + playing.boardAsString
        (playing, reply)
      case Challenged(playerPair) => (gameInstance, Strings.FailedAcceptOrReject(playerPair.defender.id))
      case _ => (gameInstance, Strings.FailedAcceptOrReject)
    }

  }

  def drop(col: String, gameInstance: GameInstance, playerRole: CellContents): (GameInstance, String) =
    gameInstance match {
      case playing @ Playing(_,_) => playIf(col.toInt, playing, playerRole)
      case _ => (gameInstance, Strings.FailedDrop)
    }

  def forfeit(gameInstance: GameInstance, playerRole: CellContents): (GameInstance, String) =
    gameInstance match {
      case playing @ Playing(_, _) => (playing.finishGame(playerRole.opposite), Strings.Forfeit)
        // TODO: Give the user more information about why the command failed
      case _ => (gameInstance, Strings.FailedForfeit)
    }


  def reject(gameInstance: GameInstance, playerRole: CellContents): (GameInstance, String) =
    gameInstance match {
      case challenged @ Challenged(_) if playerRole == Defender => (challenged.finishGame, Strings.Reject)
        // TODO: Say who's defending here
      case _ => (gameInstance, Strings.FailedAcceptOrReject)
    }

  def changeToken(gameInstance: GameInstance, message: String, playerRole: CellContents, emojiHandler: EmojiHandler): (GameInstance, String) = {
    val validatedToken = emojiHandler.validateAndExtractEmoji(message, Strings.FailedToken)
    (gameInstance.changeToken(playerRole, validatedToken), Strings.tokenChange(validatedToken))
  }

  // TODO: These last few helper functions could be better placed to reduce the bloat of this file

  // TODO: Better name for function
  // Plays a turn if the play meets all the rules
  // TODO: Could bring out the gameInstance part and have only gameState in here
  private def playIf(col: Int, playing: Playing, playerRole: CellContents): (GameInstance, String) = {

    // Check it's this players turn
    // TODO: Don't do unit here
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

}
