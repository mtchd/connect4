sealed trait GameInstance {

  def startPlaying: Playing = this match {
    case Challenged(playerPair) => {
      val gameState = GameState.newDefaultBoard()
      Playing(gameState, playerPair)
    }
    case playing @ Playing(_,_) => playing
  }

  def boardAsString: String = this match {
    case Challenged(_) => Strings.FailedRenderBoard
    case Playing(gameState, playerPair) => gameState.boardAsString(playerPair.defender, playerPair.challenger)
  }

  // Returns role of player, if they are in our pair
  def playerRole(playerId: String): Option[CellContents] = this match {

    case Challenged(_) => None
    case Playing(_, playerPair) => {
      playerId match {
        case playerPair.challenger.id => Some(Challenger)
        case playerPair.defender.id => Some(Defender)
        case _ => None
      }
    }
  }
}

case class Challenged(playerPair: PlayerPair) extends GameInstance

case class Playing(gameState: GameState, playerPair: PlayerPair) extends GameInstance
