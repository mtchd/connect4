package connect4.gamestore
import cats.effect.{Blocker, IO}
import connect4.GameInstance
import doobie.Transactor
import doobie.implicits._
import doobie.util.ExecutionContexts

// TODO: Refactor this to be functional
case class RDSGameStore(password: String) {

  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",     // driver classname
    "jdbc:postgresql://connect4.csmziitufcpp.ap-southeast-2.rds.amazonaws.com:5432/connect4",     // connect URL (driver-specific)
    "connect4",                  // user
    password,                          // password
    Blocker.liftExecutionContext(ExecutionContexts.synchronous) // just for testing
  )

  def setup(): IO[Int] = GameStoreQueries.createTable.run.transact(xa)

  def get(threadTimeStamp: String): IO[Option[GameStoreRow]] = {
    GameStoreQueries.searchWithTs(threadTimeStamp).option.transact(xa)
  }

  def put(threadTs: String, gameInstances: Vector[GameInstance]): IO[Int] = {
    gameInstances match {
      case Vector() => delete(threadTs)
      case gameInstances => upsert(threadTs, gameInstances.head)
    }
  }

  def upsert(threadTs: String, gameInstance: GameInstance): IO[Int] = {
    val gameStoreRow = GameStoreRow.convertGameInstance(gameInstance, threadTs)
    val insertQuery = GameStoreQueries.upsert(gameStoreRow)
    insertQuery.run.transact(xa)
  }

  def delete(threadTs: String): IO[Int]  = {
    GameStoreQueries.delete(threadTs).run.transact(xa)
  }

}

object RDSGameStore {
  def convertGame(maybeGameStoreRow: Option[GameStoreRow]): Vector[GameInstance] = {
    val maybeGameInstance = maybeGameStoreRow match {
      case Some(gameStoreRow) => Some(gameStoreRow.convertToGameInstance)
      case None => None
    }
    // TODO: It be the way it do because tech debt
    maybeGameInstance match {
      case Some(gameStoreInstance) => Vector(gameStoreInstance)
      case None => Vector.empty
    }
  }
}
