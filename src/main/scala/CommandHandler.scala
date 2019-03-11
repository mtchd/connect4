
object CommandHandler {

  // TODO: Side effect leak here...needs to be some way of handling these ids
  def challenge(opponentId: String, challengerId: String): ((Player, Player), String) = {

    s"Challenging <@$opponentId>...${Strings.newChallengeHelp}

    // Return some prompt to start listening for challenge response?

  }

  def accept(): Unit = {

  }
}
