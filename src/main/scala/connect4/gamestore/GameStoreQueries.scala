package connect4.gamestore

import doobie.implicits._

object GameStoreQueries {

  // insert query
  def insert(gameInstanceRow: GameInstanceRow): doobie.Update0 = {
    sql"""
         |INSERT INTO documents (
         |  ts,
         |  challengerId,
         |  defenderId,
         |  challengerToken,
         |  defenderToken,
         |  lastMovePlayerRole,
         |  lastMoveCol,
         |  lastMoveRow,
         |  board
         |)
         |VALUES (
         |  ${gameInstanceRow.timestamp},
         |  ${gameInstanceRow.challengerId},
         |  ${gameInstanceRow.defenderId},
         |  ${gameInstanceRow.challengerToken},
         |  ${gameInstanceRow.defenderToken},
         |  ${gameInstanceRow.lastMovePlayerRole},
         |  ${gameInstanceRow.lastMoveCol},
         |  ${gameInstanceRow.lastMoveRow},
         |  ${gameInstanceRow.board}
         |)
        """.stripMargin
      .update
  }
}
