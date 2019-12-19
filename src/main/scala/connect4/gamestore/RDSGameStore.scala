package connect4.gamestore
import cats.effect.{Blocker, ContextShift, IO}
import connect4.game.{Challenged, Finished, GameInstance, Playing, Ranked}
import doobie.Transactor
import doobie.implicits._
import doobie.util.ExecutionContexts
import doobie.util.transactor.Transactor.Aux

case class RDSGameStore(password: String) {

  implicit val cs: ContextShift[IO] = IO.contextShift(ExecutionContexts.synchronous)

  val xa: Aux[IO, Unit] = Transactor.fromDriverManager[IO](
    "org.postgresql.Driver",     // driver classname
    "jdbc:postgresql://connect4.csmziitufcpp.ap-southeast-2.rds.amazonaws.com:5432/connect4",     // connect URL (driver-specific)
    "connect4",                  // user
    password,                          // password
    Blocker.liftExecutionContext(ExecutionContexts.synchronous) // just for testing
  )

  def setupGameStore(): IO[Int] = GameStoreQueries.createTable.run.transact(xa)
  def setupScoreStore(): IO[Int] = ScoreStoreQueries.createTable.run.transact(xa)

  // TODO: Convert to calling this id instead of timestamp, as with other abstractions this could be a channel id
  def get(id: String): IO[Option[GameStoreRow]] = {
    GameStoreQueries.searchWithId(id).option.transact(xa)
  }

  // TODO: Level of abstraction on top of "get", not sure if needed
  def maybeGetGame(id: String): IO[Option[GameInstance]] =
    for {
      maybeGameRow <- get(id)
      maybeGameInstance = RDSGameStore.convertGame(maybeGameRow)
    } yield maybeGameInstance

  def reportScores(player1Id: String, player2Id: String): IO[List[ScoreStoreRow]] = {
    ScoreStoreQueries.reportScores(player1Id, player2Id).to[List].transact(xa)
  }

  def reportScore(playerId: String): IO[Option[ScoreStoreRow]] = {
    ScoreStoreQueries.reportScore(playerId).option.transact(xa)
  }

  def put(threadTs: String, gameInstance: GameInstance): IO[Int] = {

    gameInstance match {
      case Finished(_) => delete(threadTs)
        // TODO: Has to be a way to merge these two
      case playing @ Playing(_, _) => upsert(threadTs, playing)
      case challenged @ Challenged(_) => upsert(threadTs, challenged)
    }

  }

  def updateLoss(playerId: String): IO[Int] = {
    ScoreStoreQueries.upsertLoss(playerId).run.transact(xa)
  }

  def updateWin(playerId: String): IO[Int] = {
    ScoreStoreQueries.upsertWin(playerId).run.transact(xa)
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
  def convertGame(maybeGameStoreRow: Option[GameStoreRow]): Option[GameInstance] = {
    maybeGameStoreRow match {
      case Some(gameStoreRow) => Some(gameStoreRow.convertToGameInstance)
      case None => None
    }
  }
}
