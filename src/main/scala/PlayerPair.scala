case class PlayerPair(challenger: Player, defender: Player) {

  def roleFromPair(playerId: String): Option[CellContents] = playerId match {
    case challenger.id => Some(Challenger)
    case defender.id => Some(Defender)
    case _ => None
  }
}

object PlayerPair {

  def newPairFromIds(challengerId: String, defenderId: String): PlayerPair = {
    val challenger = Player.newDefaultPlayer(challengerId, Challenger)
    val defender = Player.newDefaultPlayer(defenderId, Defender)

    PlayerPair(challenger, defender)
  }
}