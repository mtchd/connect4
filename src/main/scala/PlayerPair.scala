case class PlayerPair(challenger: Player, defender: Player)

object PlayerPair {

  def newPairFromIds(challengerId: String, defenderId: String): PlayerPair = {
    val challenger = Player.newDefaultPlayer(challengerId, Challenger)
    val defender = Player.newDefaultPlayer(defenderId, Defender)

    PlayerPair(challenger, defender)
  }
}