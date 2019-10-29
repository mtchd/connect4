package connect4.gamestore

import connect4.GameInstance

trait GameStore {

  def get(threadId: String): List[GameInstance]

  def put(threadId: String, gameInstances: List[GameInstance]): Unit

}
