package connect4

import cats.effect.IO
import doobie.Transactor
import doobie.util.ExecutionContexts

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
