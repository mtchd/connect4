package connect4.game

import connect4.Strings

sealed trait GameInstance {

  val instancePlayerPair: PlayerPair

  def boardAsString: String = this match {
    case Playing(gameState, playerPair) => gameState.boardAsString(playerPair.defender, playerPair.challenger)
    case _ => Strings.FailedRenderBoard
  }

  // Returns role of player, if they are in our pair
  def playerRole(playerId: String): Option[CellContents] = this match {
    case Challenged(playerPair) => playerPair.roleFromPair(playerId)
    case Playing(_, playerPair) => playerPair.roleFromPair(playerId)
    case Finished(playerPair, _) => playerPair.roleFromPair(playerId)
  }

  def changeToken(role: CellContents, token: String): GameInstance = {
    val newPlayerPair = instancePlayerPair.updateToken(role, token)

    this match {
      case challenged @ Challenged(_) => challenged.copy(instancePlayerPair = newPlayerPair)
      case playing @ Playing(_, _) => playing.copy(instancePlayerPair = newPlayerPair)
      case finished @ Finished(_, _) => finished.copy(instancePlayerPair = newPlayerPair)
    }
  }

}

case class Challenged(instancePlayerPair: PlayerPair) extends GameInstance {

  def startPlayingWithDefenderToken(token: String): Playing = {
      // Update the playerPair with the defender token
      Playing(GameState.newDefaultBoard(), instancePlayerPair.updateDefenderToken(token))
  }

}

case class Playing(gameState: GameState, instancePlayerPair: PlayerPair) extends GameInstance

case class Finished(instancePlayerPair: PlayerPair, winner: CellContents) extends GameInstance

object GameInstance {
  def newChallenge(defenderId: String, challengerId: String, challengerToken: String): Challenged = {
    val pair = PlayerPair.newPairFromIdsWithChallengerToken(challengerId, defenderId, challengerToken)
    Challenged(pair)
  }
}
