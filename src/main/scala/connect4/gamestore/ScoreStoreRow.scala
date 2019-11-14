package connect4.gamestore

case class ScoreStoreRow(playerId: String, wins: Int, losses: Int) {
  override def toString: String = {
    s"<@$playerId> has $wins wins and $losses losses!"
  }
}
