case class PlayerPair(challenger: Player, defender: Player) {

  def roleFromPair(playerId: String): Option[CellContents] = playerId match {
    case challenger.id => Some(Challenger)
    case defender.id => Some(Defender)
    case _ => None
  }

  def updateDefenderToken(token: String): PlayerPair = {
    val defender = Player(this.defender.id, token)
    PlayerPair(this.challenger, defender)
  }
}

object PlayerPair {

  def newDefaultPairFromIds(challengerId: String, defenderId: String): PlayerPair = {
    val challenger = Player.newDefaultPlayer(challengerId, Challenger)
    val defender = Player.newDefaultPlayer(defenderId, Defender)

    PlayerPair(challenger, defender)
  }

  def newPairFromIdsWithChallengerToken(challengerId: String, defenderId: String, token: String): PlayerPair = {
    val challenger = Player(challengerId, token)
    val defender = Player.newDefaultPlayer(defenderId, Defender)

    PlayerPair(challenger, defender)
  }
}