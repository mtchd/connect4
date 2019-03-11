
object CommandHandler {

  // TODO: Side effect leak here...needs to be some way of handling these ids
  // TODO: In future, we give the ability to customise token
  // Adds the challenging and defending players to list of games in initiation, and acknowledges with message.
  def challenge(challengePairs: List[PlayerPair], defenderId: String, challengerId: String): (List[PlayerPair], String) = {

    // TODO: Maybe a for-comprehension is better here
    // This could be done in one line, but I've spaced it out here for better readability

    val challenger = Player.newDefaultPlayer(challengerId, Challenger)
    val defender = Player.newDefaultPlayer(defenderId, Defender)

    val pair = PlayerPair(challenger, defender)

    val newChallengePairs = challengePairs :+ pair

    val reply = s"Challenging <@$defenderId>...${Strings.newChallengeHelp}"

    (newChallengePairs, reply)

  }

  // TODO: Best way to format this?
  def accept(gameInstances: GameInstance, challengePairs: List[PlayerPair], accepterId: String):
  (List[GameInstance], List[PlayerPair], String) = {

    // TODO: Fix this, we shouldn't need this var to do this logic
    var foundOne = false

    // Check the player is part of a pair
    challengePairs.foreach( pair =>
      // If true, delete pair and make game
      if (pair.defender.id == accepterId) {

        val gameState = GameState.newDefaultBoard()
        val gameInstance = GameInstance(gameState, pair)
        val newGameInstances = gameInstances :+ gameInstance
        foundOne = true
      }
    )

    if (!foundOne) {
    // If not, tell player they stupid
    }


    (List.empty, List.empty, "")
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
