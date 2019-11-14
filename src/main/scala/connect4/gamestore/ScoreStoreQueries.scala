package connect4.gamestore

import connect4.game.{Finished, Ranked}
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
         |SET wins = leaderboard.wins + 1
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
         |SET losses = leaderboard.losses + 1
        """.stripMargin
      .update
  }

  def reportScores(challengerId: String, defenderId: String): doobie.Query0[ScoreStoreRow] = {
        sql"""
             |SELECT * FROM leaderboard
             |WHERE playerId = $challengerId
             |OR playerId = $defenderId
           """.stripMargin
          .query[ScoreStoreRow]
  }

}
