import Main.{nBoardCols, nBoardRows}
import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.config.ConfigFactory
import slack.SlackUtil
import slack.models.Message
import slack.rtm.SlackRtmClient

import scala.concurrent.ExecutionContextExecutor

// Gives us the regex for matching message text
import CommandsRegex._

/**
  * Bad boi that handles all interactions with slack.
  */
object SlackClient {

  implicit val system: ActorSystem = ActorSystem("slack")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  val client = SlackRtmClient(ConfigFactory.load().getString("secrets.slackApiKey"))
  val selfId: String = client.state.self.id

  def startListening(): Unit = {

    // Currently, this starter stops listening after a single game is started. In future, it will continue listening
    // but just block out the players playing.
    var handler = handleForHandler()
    handler = client.onMessage { message =>

      val mentionedIds = SlackUtil.extractMentionedIds(message.text)

      if(mentionedIds.contains(selfId)) {

        message.text match {
          case Start(_, opponent, _) => challenge(message, opponent)
          // TODO: Remove repeated code
          case Help(_) => client.sendMessage(message.channel, s"<@${message.user}>: ${Strings.help}")
          case _ => client.sendMessage(message.channel, s"<@${message.user}>: ${Strings.help}")
        }
      }
    }
  }


  // TODO: Need to make challenge stuff all happens in the one channel, otherwise it's chaos.
  /**
    * Starts new game and gets ready to recieve messages for it.
    * @param challengeMessage Challenging message that initiated game.
    * @param opponent Slack id of opponent challenged.
    */
  def challenge(challengeMessage: Message, opponent: String): Unit = {

    // TODO: Give these messages buttons for users to press.
    client.sendMessage(challengeMessage.channel,
      s"<@${challengeMessage.user}>: Challenging <@$opponent>...${Strings.newChallengeHelp}")

    // Get reference so we can stop listening after player responds to challenge
    var handler = handleForHandler()
    handler = client.onMessage { acceptMessage =>

      if (acceptMessage.user == opponent) {

        acceptMessage.text match {
          case Accept(_) =>
            newGame(acceptMessage, challengeMessage)
            client.removeEventListener(handler)
          case Reject(_) =>
            client.sendMessage(challengeMessage.channel, s"<@${challengeMessage.user}> Rejected!")
            client.removeEventListener(handler)
          case _ =>
            client.sendMessage(acceptMessage.channel, s"<@${acceptMessage.user}>: ${Strings.challengeHelp}")
        }

      }
    }
  }

  // TODO: Games can be played in their own thread for cleanlisness
  def newGame(acceptMessage: Message, challengeMessage: Message): Unit = {

    // Create players and feed them in
    // Currently hardcoded tokens
    val challenger = new Player(challengeMessage.user, Strings.challengerToken)
    val defender = new Player(acceptMessage.user, Strings.defenderToken)
    // Game is set in channel where the defender accepts it
    val gameState = new GameState(nBoardRows, nBoardCols, challenger, defender, acceptMessage.channel, true)

    client.sendMessage(acceptMessage.channel, "Available commands:\n'Drop $columnNumber'\n'forfeit'\n'reset'")
    playTurn(gameState)

  }

  // TODO: Need to handle putting in a wrong column.
  def playTurn(gameState: GameState): Unit = {

    // Check for a winner and announce if so
    val winner = gameState.checkWin()
    if (winner.isDefined) {
      client.sendMessage(gameState.channel, s"<@${winner.get.slackId}> wins!" + "\n" + ":trophy:"*15)
      // End Game
      return
    }

    // Print board at the start of a turn
    client.sendMessage(gameState.channel, gameState.boardAsString())

    // Now need to start listening to message pertinent to this game.
    // Assume a user only has one game going in a channel at any one time.
    // Now, whenever that user messages us, we assume they are talking about this game.
    var handler = handleForHandler()
    handler = client.onMessage{ newMessage =>

      if(newMessage.user == gameState.challenger.slackId || newMessage.user == gameState.defender.slackId) {

        newMessage.text match {

          // Drop a disc/token into a column
          // TODO: Break this up a bit more, this function is huge
          // TODO: Turn checking could be put into the gameState, not here for better encapsulation
          case Drop(_, col) =>

            // If new state isn't defined, it'll call userError and send a message to the user.
            val newState = gameState.playMove(col.toInt, newMessage.user)
            if (newState.isDefined) {
              client.sendMessage(newMessage.channel, s"<@${newMessage.user}>: Dropping into column $col")
              client.removeEventListener(handler)
              // Play the move, which gives an updated game state. Also switches whose turn it is.
              playTurn(newState.get)
            }
            // Else do nothing and keep listening, the move was invalid and it's still that players turn.

          case Stop(_) =>
            client.sendMessage(newMessage.channel, s"<@${newMessage.user}>: Forfeiting game...")
            client.removeEventListener(handler)
          case Reset(_) =>
            client.sendMessage(newMessage.channel, s"<@${newMessage.user}>: Forfeiting and resetting game...")
            client.removeEventListener(handler)
            playTurn(
              new GameState(
                nBoardRows,
                nBoardCols,
                gameState.challenger,
                gameState.defender,
                newMessage.channel,
                defendersTurn = true
              )
            )
          case _ =>
            // TODO: Help message for those that try to play but aren't part of the game.
            // Do nothing, as they could have sent any message, as we are no longer disambiguating via @connect4 bot
        }

      }
    }
  }

  def handleForHandler(): ActorRef = {
    // This is weird as hell but it works at least. Creating this reference allows us to call removeEventListener
    // inside the next handler function.
    // TODO: Find a way of doing this that isn't so hacked.
    client.onMessage { message =>
      // Do nothing
    }
  }

  def messageUser(message: String, channel: String, slackId: String): Unit = {
    client.sendMessage(channel, s"<@$slackId>: $message")
  }
}
