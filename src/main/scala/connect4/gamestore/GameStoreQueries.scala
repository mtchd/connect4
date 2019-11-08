package connect4.gamestore

import cats.data.NonEmptyList
import doobie.implicits._
import doobie.util.{Meta, Put}
import io.circe.Json
import io.circe.parser._
import org.postgresql.util.PGobject

object GameStoreQueries {

  def createTable = {
    sql"""
         |CREATE TABLE IF NOT EXISTS connect4 (
         |  ts VARCHAR(100) PRIMARY KEY,
         |  challengerId VARCHAR(100),
         |  defenderId VARCHAR(100),
         |  challengerToken VARCHAR(100),
         |  defenderToken VARCHAR(100),
         |  lastMovePlayerRole VARCHAR(100),
         |  lastMoveCol VARCHAR(100),
         |  lastMoveRow VARCHAR(100),
         |  board JSON
         |)
       """.stripMargin
      .update
  }

  implicit val jsonMeta: Meta[Json] =
    Meta.Advanced.other[PGobject]("json").timap[Json](
      a => parse(a.getValue).left.map[Json](e => throw e).merge)(
      a => {
        val o = new PGobject
        o.setType("json")
        o.setValue(a.noSpaces)
        o
      }
    )

  // insert query
  def insert(gameInstanceRow: GameInstanceRow): doobie.Update0 = {
    sql"""
         |INSERT INTO connect4 (
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
         |ON CONFLICT (ts)
         |DO UPDATE
         |SET challengerToken = ${gameInstanceRow.challengerToken},
         |    defenderToken = ${gameInstanceRow.defenderToken},
         |    lastMovePlayerRole = ${gameInstanceRow.lastMovePlayerRole},
         |    lastMoveCol = ${gameInstanceRow.lastMoveCol},
         |    lastMoveRow = ${gameInstanceRow.lastMoveRow},
         |    board = ${gameInstanceRow.board}
        """.stripMargin
      .update
  }

  // search id
  def searchWithTs(ts: String): doobie.Query0[GameInstanceRow] = {
    sql"""
         |SELECT * FROM connect4
         |WHERE ts = $ts
         |LIMIT 1
       """.stripMargin
      .query[GameInstanceRow]
  }
}
