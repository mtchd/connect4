package connect4.game

import connect4.Strings

sealed trait GameInstance {

  // TODO: Cause soft fail for a lot of these finish things

  def boardAsString: String = this match {
    case Playing(gameState, playerPair) => gameState.boardAsString(playerPair.defender, playerPair.challenger)
    case _ => Strings.FailedRenderBoard
  }

  // Returns role of player, if they are in our pair
  def playerRole(playerId: String): Option[PlayerRole] = this match {
    case Challenged(playerPair) => playerPair.roleFromPair(playerId)
    case Playing(_, playerPair) => playerPair.roleFromPair(playerId)
    case _ => None
  }

  def changeToken(role: CellContents, token: String): GameInstance = {

    this match {
      case challenged @ Challenged(playerPair) => challenged.copy(instancePlayerPair = playerPair.updateToken(role, token))
      case playing @ Playing(_, playerPair) => playing.copy(instancePlayerPair = playerPair.updateToken(role, token))
      case finished @ Finished(_) => finished
    }
  }

}

// TODO: Should theses case classes have their own file?
case class Challenged(instancePlayerPair: PlayerPair) extends GameInstance {

  def startPlayingWithDefenderToken(token: String): Playing = {
      // Update the playerPair with the defender token
      Playing(GameState.newDefaultBoard(), instancePlayerPair.updateDefenderToken(token))
  }

  def finishGame: Finished = Finished(UnRanked)

}

case class Playing(gameState: GameState, instancePlayerPair: PlayerPair) extends GameInstance {
  def finishGame(winnerRole: PlayerRole): Finished = Finished.finishRanked(instancePlayerPair, winnerRole)
}

// TODO: This is completely different from challenged and playing and needs to be separated, either by a higher level
// of abstraction or by making it a complete different type
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
