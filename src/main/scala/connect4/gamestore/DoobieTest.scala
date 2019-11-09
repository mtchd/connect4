package connect4.gamestore

import cats.effect._
import connect4.{Challenged, PlayerPair}
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

    GameStoreQueries.createTable.run.transact(xa).unsafeRunSync()

    val playerPair = PlayerPair.newTestPair()

    val oo = Challenged(playerPair)
    val ooo = Vector(oo)

    RDSGameStore.put("11", ooo)

    println(RDSGameStore.get("11"))

  }

}
