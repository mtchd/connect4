package connect4.gamestore

import connect4.game.{Cell, Challenged, GameInstance, GameState, Move, PlayerPair, Playing, UnFinishedGame}
import io.circe._
import io.circe.syntax._

case class GameStoreRow(
                            timestamp: String,
                            challengerId: String,
                            defenderId: String,
                            challengerToken: String,
                            defenderToken: String,
                            lastMovePlayerRole: Option[String],
                            lastMoveCol: Option[Int],
                            lastMoveRow: Option[Int],
                            board: Option[Json]
                          ) {

  def convertToGameInstance: UnFinishedGame = {
    // TODO: Just checking the presence of board seems dodgy
    board match {
      case Some(board) => convertToPlaying(board)
      case None => convertToChallenged
    }
  }

  def generatePlayerPair: PlayerPair = {
    PlayerPair.newPairWithTokens(challengerId, challengerToken, defenderId, defenderToken)
  }

  def generateLastMove(lastMovePlayerRole: String): Option[Move] = {
    val playerRole = Cell.convertFromString(lastMovePlayerRole)
    Some(Move(playerRole, lastMoveRow.get, lastMoveCol.get))
  }

  def convertFromJsonBoard(board: Json): Vector[Vector[Cell]] = {
    val result = board.as[Vector[Vector[String]]]

    result match {
      case Right(right) => GameStoreRow.convertToCellBoard(right)
        // TODO: Should we throw error here?
      case Left(error) => throw error
    }

  }

  def convertToChallenged: Challenged = {
    Challenged(generatePlayerPair)
  }

  def convertToPlaying(board: Json): Playing = {

    val playerPair = generatePlayerPair
    val maybeLastMove: Option[Move] = lastMovePlayerRole match {
      case Some(lastMovePlayerRole) => generateLastMove(lastMovePlayerRole)
      case None => None
    }
    val convertedBoard = convertFromJsonBoard(board)
    val gameState = GameState(convertedBoard, maybeLastMove)
    Playing(gameState, playerPair)
  }

}

object GameStoreRow {

  def convertGameInstance(gameInstance: UnFinishedGame, timestamp: String): GameStoreRow = {
    gameInstance match {
      case challenged @ Challenged(_) => convertGameInstance(challenged, timestamp)
      case playing @ Playing(_,_) => convertGameInstance(playing, timestamp)
    }
  }

  def convertGameInstance(gameInstance: Challenged, timeStamp: String): GameStoreRow = {

    GameStoreRow(
      timeStamp,
      gameInstance.instancePlayerPair.challenger.id,
      gameInstance.instancePlayerPair.defender.id,
      gameInstance.instancePlayerPair.challenger.token,
      gameInstance.instancePlayerPair.defender.token,
      None,
      None,
      None,
      None
    )

  }

  def convertGameInstance(gameInstance: Playing, timeStamp: String): GameStoreRow = {

    val (lastMovePlayerRole, lastMoveCol, lastMoveRow) = gameInstance.gameState.lastMove match {
      case Some(lastMove) => (Some(Cell.convertToString(lastMove.playerRole)), Some(lastMove.col), Some(lastMove.row))
      case None => (None, None, None)
    }

    GameStoreRow(
      timeStamp,
      gameInstance.instancePlayerPair.challenger.id,
      gameInstance.instancePlayerPair.defender.id,
      gameInstance.instancePlayerPair.challenger.token,
      gameInstance.instancePlayerPair.defender.token,
      lastMovePlayerRole,
      lastMoveCol,
      lastMoveRow,
      Some(convertToStringBoard(gameInstance.gameState.board).asJson)
    )

  }

  // TODO: We shouldn't need to do this if we have an encoder for Cell...maybe
  def convertToStringBoard(board: Vector[Vector[Cell]]): Vector[Vector[String]] = {
    board.map{ row =>
      row.map { cell =>
        Cell.convertToString(cell.contents)
      }
    }
  }

  def convertToCellBoard(board: Vector[Vector[String]]): Vector[Vector[Cell]] = {
    board.map{ row =>
      row.map { cell =>
        Cell(Cell.convertFromString(cell))
      }
    }
  }
}
