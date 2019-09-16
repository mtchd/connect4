import doobie._
import doobie.implicits._
import doobie.util.ExecutionContexts

import cats._
import cats.effect._
import cats.implicits._

object DatabaseClient {
  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

  val xa = Transactor.fromDriverManager[IO](
  "org.postgresql.Driver",     // driver classname
  "jdbc:postgresql:world",     // connect URL (driver-specific)
  "postgres",                  // user
  "",                          // password
  ExecutionContexts.synchronous // just for testing
)

  def program(): Unit = {
    val program2 = sql"select 42".query[Int].unique
    val io2 = program2.transact(xa)
    println(io2.unsafeRunSync)
    println("hello")
  }
}


