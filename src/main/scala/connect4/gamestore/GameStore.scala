package connect4.gamestore

import connect4.GameInstance

trait GameStore {

  def get(threadId: String): Vector[GameInstance]

  def put(threadId: String, gameInstances: Vector[GameInstance]): Unit

}
