package connect4.gamestore
import cats.effect.{Blocker, IO}
import connect4.GameInstance
import doobie.Transactor
import doobie.implicits._
import doobie.util.ExecutionContexts

object RDSGameStore extends GameStore {

  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",     // driver classname
    "connect4.csmziitufcpp.ap-southeast-2.rds.amazonaws.com",     // connect URL (driver-specific)
    "postgres",                  // user
    "",                          // password
    Blocker.liftExecutionContext(ExecutionContexts.synchronous) // just for testing
  )

  override def get(threadTimeStamp: String): Vector[GameInstance] = {
    val maybeGameStoreRow = GameStoreQueries.searchWithTs(threadTimeStamp).option.transact(xa).unsafeRunSync
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

  override def put(threadTs: String, gameInstances: Vector[GameInstance]): Unit = {
    val gameInstance = gameInstances.head

    println(gameInstance, threadTs)

    val gameStoreRow = GameStoreRow.convertGameInstance(gameInstance, threadTs)

    val insertQuery = GameStoreQueries.insert(gameStoreRow)

    insertQuery.run.transact(xa).unsafeRunSync()
  }
}
