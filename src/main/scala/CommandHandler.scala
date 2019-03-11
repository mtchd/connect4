
object CommandHandler {

  // TODO: Side effect leak here...needs to be some way of handling these ids
  def challenge(opponentId: String, challengerId: String): Unit = {

    // s"Challenging <@$opponentId>...${Strings.newChallengeHelp}

    // Return some prompt to start listening for challenge response?

  }

  def accept(): Unit = {
    /**
    // TODO: Fix this, we shouldn't need this var to do this logic
    var foundOne = false
    // Check the player is part of a pair
    challengePairs.foreach( pair =>
    // If true, delete pair and make game
    if (pair.defender.id == message.authorId.toString) {
    val (gameState, message) = CommandHandler.newGame()
    val gameInstance = new gameInstance(gameState, pair)
    gameInstances = gameInstances :+ gameInstance
    foundOne = true
    }
    )

    if (!foundOne) {
    // If not, tell player they stupid
    }
    **/
  }

  def drop: Unit = {
    /**
    gameInstances.map(gameInstance =>

    // CellContents is confusing here. We are really referring to the role of a player (challenger / defender)
    val Option[CellContents] = gameInstance.returnRoleIfExists(message.authorId)
    if (gameInstance.pairHas(message.authorId)) {
      CommandHandler.playMove(col, )
    })
      */
  }


}
