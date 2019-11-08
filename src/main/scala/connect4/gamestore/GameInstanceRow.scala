package connect4.gamestore

import connect4.{Cell, Challenged, Playing}
import io.circe._
import io.circe.syntax._

case class GameInstanceRow(
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

  def convertGameInstance(gameInstance: Playing, timeStamp: String): GameInstanceRow = {

    GameInstanceRow(
      timeStamp,
      gameInstance.instancePlayerPair.challenger.id,
      gameInstance.instancePlayerPair.defender.id,
      gameInstance.instancePlayerPair.challenger.token,
      gameInstance.instancePlayerPair.defender.token,
      Some(Cell.convertToString(gameInstance.gameState.lastMove.orNull.playerRole)),
      Some(gameInstance.gameState.lastMove.orNull.col),
      Some(gameInstance.gameState.lastMove.orNull.row),
      Some(convertBoard(gameInstance.gameState.board).asJson)
    )

  }

  def convertBoard(board: Vector[Vector[Cell]]): Vector[Vector[String]] = {

    board.map{ row =>
      row.map { cell =>
        Cell.convertToString(cell.contents)
      }
    }
  }
}

object GameInstanceRow {
  def convertGameInstance(gameInstance: Challenged, timeStamp: String): GameInstanceRow = {

    GameInstanceRow(
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
}
