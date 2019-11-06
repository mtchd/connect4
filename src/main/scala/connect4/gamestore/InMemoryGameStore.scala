package connect4.gamestore

import connect4.GameInstance

case class InMemoryGameStore(private var threadAndGameInstances: Map[String, Vector[GameInstance]] = Map.empty) extends GameStore {
  override def get(threadId: String): Vector[GameInstance] = threadAndGameInstances.getOrElse(threadId, Vector.empty)

  override def put(threadId: String, gameInstances: Vector[GameInstance]): Unit = {
    threadAndGameInstances = threadAndGameInstances.updated(threadId, gameInstances)
  }
}
