package connect4.gamestore
import cats.effect.{Blocker, IO}
import connect4.GameInstance
import doobie.Transactor
import doobie.implicits._
import doobie.util.ExecutionContexts

case class RDSGameStore(password: String) extends GameStore {

  implicit val cs = IO.contextShift(ExecutionContexts.synchronous)

  val xa = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",     // driver classname
    "jdbc:postgresql://connect4.csmziitufcpp.ap-southeast-2.rds.amazonaws.com:5432/connect4",     // connect URL (driver-specific)
    "connect4",                  // user
    password,                          // password
    Blocker.liftExecutionContext(ExecutionContexts.synchronous) // just for testing
  )

  def setup(): Unit = {
    println(GameStoreQueries.createTable.run.transact(xa).unsafeRunSync())
  }

  override def get(threadTimeStamp: String): Vector[GameInstance] = {

    println(threadTimeStamp)

    val maybeGameStoreRow = GameStoreQueries.searchWithTs(threadTimeStamp).option.transact(xa).unsafeRunSync

    println(maybeGameStoreRow)

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
