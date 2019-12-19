package connect4.game

import connect4.Strings

sealed trait GameInstance

sealed trait UnFinishedGame extends GameInstance {

  val instancePlayerPair: PlayerPair

  def maybePlayerRole(playerId: String): Option[PlayerRole] = this match {
    case Challenged(playerPair) => playerPair.roleFromPair(playerId)
    case Playing(_, playerPair) => playerPair.roleFromPair(playerId)
  }

  def changeToken(role: PlayerRole, token: String): GameInstance =
    this match {
      case challenged @ Challenged(playerPair) => challenged.copy(instancePlayerPair = playerPair.updateToken(role, token))
      case playing @ Playing(_, playerPair) => playing.copy(instancePlayerPair = playerPair.updateToken(role, token))
    }
}

// TODO: Should theses case classes have their own file (note this would mean not using sealed)
case class Challenged(instancePlayerPair: PlayerPair) extends UnFinishedGame {

  def startPlayingWithDefenderToken(token: String): Playing =
      Playing(GameState.newDefaultBoard(), instancePlayerPair.updateDefenderToken(token))

  def finishGame: Finished = Finished(UnRanked)

}

case class Playing(gameState: GameState, instancePlayerPair: PlayerPair) extends UnFinishedGame {

  def finishGame(winnerRole: PlayerRole): Finished = Finished.finishRanked(instancePlayerPair, winnerRole)

  def boardAsString: String = gameState.boardAsString(instancePlayerPair.defender, instancePlayerPair.challenger)
}

case class Finished(rankType: RankType) extends GameInstance

// TODO: Actually let the player choose Ranked or Unranked
sealed trait RankType
case class Ranked(winnerId: String, loserId: String) extends RankType
case object UnRanked extends RankType

object Finished {
  def finishRanked(playerPair: PlayerPair, winnerRole: PlayerRole): Finished = {
    val (winner, loser) = playerPair.winnerAndLoserIds(winnerRole)
    Finished(Ranked(winner, loser))
  }
}

object GameInstance {
  def newChallenge(defenderId: String, challengerId: String, challengerToken: String): Challenged = {
    val pair = PlayerPair.newPairFromIdsWithChallengerToken(challengerId, defenderId, challengerToken)
    Challenged(pair)
  }
}
