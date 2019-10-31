package connect4.gamestore

import connect4.GameInstance
import doobie.implicits._

object GameStoreQueries {

  // Setup queries
  def createDb = {
    sql"""
         |CREATE DATABASE IF NOT EXISTS connect4
       """
      .update
  }

  def createTable = {
    sql"""
         |CREATE TABLE IF NOT EXISTS documents (
         |  thread VARCHAR(100) PRIMARY KEY,
         |  name VARCHAR(100),
         |  timestamp Long
         |)
       """.stripMargin
      .update
  }

  def convertGameInstance(gameInstance: GameInstance): Unit = {

  }

}
