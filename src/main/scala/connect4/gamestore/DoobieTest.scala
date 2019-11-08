package connect4.gamestore

import cats.effect._
import cats.implicits._
import connect4.{Challenged, Defender, PlayerPair}
import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts

object DoobieTest {

  def test(): Unit = {

    implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

    val xa = Transactor.fromDriverManager[IO](
      "org.postgresql.Driver",     // driver classname
      "jdbc:postgresql:world",     // connect URL (driver-specific)
      "postgres",                  // user
      "",                          // password
      Blocker.liftExecutionContext(ExecutionContexts.synchronous) // just for testing
    )

    val program3a = {
      val a: ConnectionIO[Int] = sql"select 42".query[Int].unique
      val b: ConnectionIO[Double] = sql"select random()".query[Double].unique
      (a, b).tupled
    }

    GameStoreQueries.createTable.run.transact(xa).unsafeRunSync()

    val playerPair = PlayerPair.newTestPair()

    val newPlayerPair = playerPair.updateToken(Defender, ":white_circle:")

    val thing = GameInstanceRow.convertGameInstance(Challenged(newPlayerPair), "11")

    println(thing)

    val ooo = GameStoreQueries.insert(thing)

    val oohhh = ooo.run.transact(xa)

    println(ooo)

    println(oohhh)

    println(oohhh.unsafeRunSync)

    val valuesList: doobie.ConnectionIO[List[(Int, Double)]] = program3a.replicateA(5)

    val result = valuesList.transact(xa)

    result.unsafeRunSync.foreach(println)

    println(GameStoreQueries.searchWithTs("11").option.transact(xa).unsafeRunSync)
  }

}
