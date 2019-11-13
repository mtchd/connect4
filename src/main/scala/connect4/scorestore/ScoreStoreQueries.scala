package connect4.scorestore

import doobie.implicits._

object ScoreStoreQueries {

  def createTable: doobie.Update0 = {
    sql"""
         |CREATE TABLE IF NOT EXISTS leaderboard (
         |  playerId VARCHAR(100) PRIMARY KEY,
         |  wins INT,
         |  losses INT
         |)
       """.stripMargin
      .update
  }

  // TODO: Merge upsertWin and upsertLoss somehow...
  def upsertWin(playerId: String): doobie.Update0 = {
    sql"""
         |INSERT INTO leaderboard (
         |  playerId,
         |  wins,
         |  losses
         |)
         |VALUES (
         |  ${playerId},
         |  1,
         |  0
         |)
         |ON CONFLICT (playerId)
         |DO UPDATE
         |SET wins = wins + 1
        """.stripMargin
      .update
  }

  def upsertLoss(playerId: String): doobie.Update0 = {
    sql"""
         |INSERT INTO leaderboard (
         |  playerId,
         |  wins,
         |  losses
         |)
         |VALUES (
         |  ${playerId},
         |  0,
         |  1
         |)
         |ON CONFLICT (playerId)
         |DO UPDATE
         |SET losses = losses + 1
        """.stripMargin
      .update
  }

//  def searchWithTs(playerId: String): doobie.Query0[GameStoreRow] = {
//    sql"""
//         |SELECT * FROM leaderboard
//         |WHERE playerId = $playerId
//         |LIMIT 1
//       """.stripMargin
//      .query[GameStoreRow]
//  }


}
