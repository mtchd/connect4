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

  /**
    * Start point of the program, handles all incoming messages in channels the bot is present in.
    */
  def startListening(): Unit = {

    client.onMessage { message =>

      val mentionedIds = SlackUtil.extractMentionedIds(message.text)

      if(mentionedIds.contains(selfId)) {

        message.text match {
          case Start(_, opponent, _) => challenge(message, opponent)
          case _ => client.sendMessage(message.channel, s"<@${message.user}>: ${Strings.help}")
        }
      }
    }
  }


  //TODO: Need to make challenge stuff all happens in the one channel, otherwise it's chaos.
  // This can now only happen if there is another thread somewhere with the exact same time stamp.
  /**
    * Starts new game and gets ready to recieve messages for it.
    * @param challengeMessage Challenging message that initiated game.
    * @param opponentId Slack id of opponent challenged.
    */
  def challenge(challengeMessage: Message, opponentId: String): Unit = {

    //TODO: Give these messages buttons for users to press.
    // Looks like you can do it through Dialog.scala under slack-scala-client/src/main/scala/slack/models/
    client.sendMessage(
      challengeMessage.channel,
      s"<@${challengeMessage.user}>: Challenging <@$opponentId>...${Strings.newChallengeHelp}",
      Some(challengeMessage.ts)
    )

    // Get reference so we can stop listening after player responds to challenge
    var handler = handleForHandler()
    handler = client.onMessage { acceptMessage =>
      challengeResponse(acceptMessage, challengeMessage, opponentId, handler)
    }
  }

  def challengeResponse(acceptMessage: Message, challengeMessage: Message, opponentId: String, handler: ActorRef
                       ): Unit = {

    val thread = acceptMessage.thread_ts.getOrElse{ return }

    if (thread == challengeMessage.ts) {
      if (acceptMessage.user == opponentId) {

        acceptMessage.text match {
          case Accept(_) =>
            client.removeEventListener(handler)
            newGame(acceptMessage, challengeMessage)
          case Reject(_) =>
            client.removeEventListener(handler)
            client.sendMessage(challengeMessage.channel, s"<@${challengeMessage.user}> Rejected!", Some(thread))
          case _ =>
            client.sendMessage(
              acceptMessage.channel,
              s"<@${acceptMessage.user}>: ${Strings.challengeHelp}",
              Some(thread))
        }
      }

      else {
        client.sendMessage(
          challengeMessage.channel,
          s"<@${acceptMessage.user}>: You have not been challenged, although you do seem mentally challenged.",
          Some(thread))
      }
    }
  }

  // TODO: Games can be played in their own thread for cleanliness
  def newGame(acceptMessage: Message, challengeMessage: Message): Unit = {

    // Create players and feed them in
    // Currently hardcoded tokens
    val challenger = new Player(challengeMessage.user, Strings.challengerToken)
    val defender = new Player(acceptMessage.user, Strings.defenderToken)
    // Game is set in channel where the defender accepts it
    val slackGameState = new SlackGameState(acceptMessage.channel, acceptMessage.thread_ts, challenger, defender,true)

    client.sendMessage(
      acceptMessage.channel,
      "Available commands:\n'Drop $columnNumber'\n'forfeit'\n'reset'",
      acceptMessage.thread_ts
    )

    playTurn(slackGameState)

  }

  def playTurn(slackGameState: SlackGameState): Unit = {

    // Check for a winner and announce if so
    val winner = slackGameState.checkWin()
    if (winner.isDefined) {
      // TODO: Create function to shorten this sendMessage business
      client.sendMessage(slackGameState.channel, s"<@${winner.get.slackId}> wins!" + "\n" + ":trophy:"*15, slackGameState.thread_ts)
      // End Game
      return
    }

    // Print board at the start of a turn
    client.sendMessage(slackGameState.channel, slackGameState.gameState.boardAsString())

    // Now need to start listening to message pertinent to this game.
    // Assume a user only has one game going in a channel at any one time.
    // Now, whenever that user messages us, we assume they are talking about this game.
    var handler = handleForHandler()
    handler = client.onMessage{ newMessage =>

      if(newMessage.user == slackGameState.challenger.slackId || newMessage.user == slackGameState.defender.slackId) {

        newMessage.text match {

          // Drop a disc/token into a column
          // TODO: Break this up a bit more, this function is huge
          // TODO: Turn checking could be put into the gameState, not here for better encapsulation
          case Drop(_, col) =>

            // If new state isn't defined, it'll call userError and send a message to the user.
            val newState = slackGameState.playMove(col.toInt, newMessage.user)
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
            // TODO: Make this neater by creating reset game function in SlackGameState
            playTurn(
              new SlackGameState(
                slackGameState.gameState.nBoardRows,
                slackGameState.gameState.nBoardCols,
                slackGameState.channel,
                slackGameState.thread_ts,
                slackGameState.challenger,
                slackGameState.defender,
                defendersTurn = true
              )
            )
          case _ =>
            // TODO: Help message for those that try to play but aren't part of the game.
            // Do nothing, as they could have sent any message, as we are no longer disambiguating via @connect4 bot
            // We will disambiguate via threads, so will need to update
        }

      }
    }
  }

  def handleForHandler(): ActorRef = {
    // This is weird as hell but it works at least. Creating this reference allows us to call removeEventListener
    // inside the next handler function.
    // TODO: Find a way of doing this that isn't so hacked.
    client.onMessage { _ => /* Do nothing */ }
  }

  def messageUser(message: String, channel: String, thread_ts: Option[String], slackId: String): Unit = {
    client.sendMessage(channel, s"<@$slackId>: $message", thread_ts)
  }
}
