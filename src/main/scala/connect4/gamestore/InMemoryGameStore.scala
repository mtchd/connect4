package connect4.gamestore

import connect4.GameInstance

case class InMemoryGameStore(private var threadAndGameInstances: Map[String, List[GameInstance]] = Map.empty) extends GameStore {
  override def get(threadId: String): List[GameInstance] = threadAndGameInstances.getOrElse(threadId, List.empty)

  override def put(threadId: String, gameInstances: List[GameInstance]): Unit = {
    threadAndGameInstances = threadAndGameInstances.updated(threadId, gameInstances)
  }
}
