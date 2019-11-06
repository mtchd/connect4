package connect4.gamestore

import doobie._
import doobie.implicits._
import cats._
import cats.effect._
import cats.implicits._
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

    val valuesList = program3a.replicateA(5)

    val result = valuesList.transact(xa)

    result.unsafeRunSync.foreach(println)
  }

}
