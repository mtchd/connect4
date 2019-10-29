package connect4.gamestore

import connect4.GameInstance

trait GameStore {

  // ThreadId => List[connect4.GameInstance]
  def get(threadId: String): List[GameInstance]

  // Persist state (ThreadId, List[connect4.GameInstance]) => Unit

}
