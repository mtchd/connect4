package connect4.game

import connect4.Strings

case class PlayerPair(challenger: Player, defender: Player) {

  def roleFromPair(playerId: String): Option[PlayerRole] =
    playerId match {
      case challenger.id => Some(Challenger)
      case defender.id => Some(Defender)
      case _ => None
    }

  def updateDefenderToken(token: String): PlayerPair = {
    val defender = Player(this.defender.id, token)
    PlayerPair(this.challenger, defender)
  }

  def updateToken(role: PlayerRole, token: String): PlayerPair =
    role match {
      case Defender => this.copy(defender = defender.copy(token = token))
      case Challenger => this.copy(challenger = challenger.copy(token = token))
    }

  def isPlayerInPair(playerId: String): Boolean = {
    roleFromPair(playerId).isDefined
  }

  def atLeastOneInPair(challengerId: String, defenderId: String): Boolean =
    isPlayerInPair(challengerId) || isPlayerInPair(defenderId)

  def winnerAndLoserIds(winnerRole: PlayerRole): (String, String) =
    winnerRole match {
      case Challenger => (challenger.id, defender.id)
      case Defender => (defender.id, challenger.id)
    }
}

object PlayerPair {

  def newDefaultPairFromIds(challengerId: String, defenderId: String): PlayerPair =
    newPairWithTokens(challengerId, Strings.DefaultChallengerToken, defenderId, Strings.DefaultDefenderToken)

  def newTestPair(): PlayerPair = {
    newDefaultPairFromIds("1","2")
  }

  def newPairFromIdsWithChallengerToken(challengerId: String, defenderId: String, token: String): PlayerPair = {
    val challenger = Player(challengerId, token)
    val defender = Player(defenderId, Strings.DefaultDefenderToken)
    PlayerPair(challenger, defender)
  }

  def newPairWithTokens(challengerId: String, challengerToken: String, defenderId: String, defenderToken: String): PlayerPair = {
    val defender = Player(defenderId, defenderToken)
    val challenger = Player(challengerId, challengerToken)
    PlayerPair(challenger, defender)
  }
}