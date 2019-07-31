sealed trait GameInstance {

  def startPlaying: Playing = this match {
    case Challenged(playerPair) => {
      val gameState = GameState.newDefaultBoard()
      Playing(gameState, playerPair)
    }
    case playing @ Playing(_,_) => playing
  }

  def startPlayingWithDefenderToken(token: String): Playing = this match{
    case Challenged(playerPair) => {
      // Update the playerPair with the defender token
      val newPlayerPair = playerPair.updateDefenderToken(token)
      val gameState = GameState.newDefaultBoard()
      Playing(gameState, newPlayerPair)
    }
    case playing @ Playing(_,_) => playing
  }

  def boardAsString: String = this match {
    case Challenged(_) => Strings.FailedRenderBoard
    case Playing(gameState, playerPair) => gameState.boardAsString(playerPair.defender, playerPair.challenger)
  }

  // Returns role of player, if they are in our pair
  def playerRole(playerId: String): Option[CellContents] = this match {

    case Challenged(playerPair) => playerPair.roleFromPair(playerId)
    case Playing(_, playerPair) => playerPair.roleFromPair(playerId)
  }

}

case class Challenged(playerPair: PlayerPair) extends GameInstance

case class Playing(gameState: GameState, playerPair: PlayerPair) extends GameInstance
