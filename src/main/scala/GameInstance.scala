case class GameInstance(gameState: GameState, playerPair: PlayerPair) {

  // Returns role of player, if they are in our pair
  def has(playerId: String): Option[CellContents] = {

    playerId match {
      case playerPair.challenger.id => Some(Challenger)
      case playerPair.defender.id => Some(Defender)
      case _ => None
    }

  }

}
