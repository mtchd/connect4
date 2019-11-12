package connect4.game

import connect4.{Strings, game}

sealed trait GameInstance {

  val instancePlayerPair: PlayerPair

  def startPlaying: Playing = this match {
    case Challenged(playerPair) => {
      val gameState = GameState.newDefaultBoard()
      game.Playing(gameState, playerPair)
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

  def changeToken(role: CellContents, token: String): GameInstance = {
    val newPlayerPair = instancePlayerPair.updateToken(role, token)

    this match {
      case challenged @ Challenged(_) => challenged.copy(instancePlayerPair = newPlayerPair)
      case playing @ Playing(_, _) => playing.copy(instancePlayerPair = newPlayerPair)
    }
  }

  def playing: Option[Playing] = this match {
    case playing @ Playing(_, _) => Some(playing)
    case _ => None
  }
}

case class Challenged(instancePlayerPair: PlayerPair) extends GameInstance

case class Playing(gameState: GameState, instancePlayerPair: PlayerPair) extends GameInstance

object GameInstance {
  def newChallenge(defenderId: String, challengerId: String, challengerToken: String): Challenged = {
    val pair = PlayerPair.newPairFromIdsWithChallengerToken(challengerId, defenderId, challengerToken)
    Challenged(pair)
  }
}
