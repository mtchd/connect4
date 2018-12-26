import Main.{nBoardCols, nBoardRows, playerO, playerX}
import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.config.ConfigFactory
import slack.SlackUtil
import slack.models.Message
import slack.rtm.SlackRtmClient

import scala.concurrent.ExecutionContextExecutor

import CommandsRegex._

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

      // Checking that they have done @connect4 bot may not really be nessacary. They could just say the message...
      if(mentionedIds.contains(selfId)) {

        message.text match {
          case Start(_, opponent, _) => challenge(message, opponent)
            client.removeEventListener(handler)
          case _ => client.sendMessage(message.channel, s"<@${message.user}>: I don't understand...")
        }
      }
    }
  }


  /**
    * Starts new game and gets ready to recieve messages for it.
    * @param challengeMessage Challenging message that initiated game.
    * @param opponent Slack id of opponent challenged.
    */
  def challenge(challengeMessage: Message, opponent: String): Unit = {

    // TODO: Give these messages buttons for users to press.
    val instructions = "\nThey must respond with 'accept' or 'reject.'"
    client.sendMessage(challengeMessage.channel,
      s"<@${challengeMessage.user}>: Challenging <@$opponent>...$instructions")

    // Stops listening after player responds to challenge
    var handler = handleForHandler()
    handler = client.onMessage { acceptMessage =>

      val mentionedIds = SlackUtil.extractMentionedIds(acceptMessage.text)

      if (mentionedIds.contains(selfId) && acceptMessage.user == opponent) {

        acceptMessage.text match {
          case Accept(_, challengeMessage.user, _) => newGame(acceptMessage, challengeMessage)
            client.removeEventListener(handler)
          case Accept(_, acceptMessage.user, _) =>
            client.sendMessage(acceptMessage.channel, s"<@${acceptMessage.user}: ${Strings.acceptSelf}")
          case Reject(_, challengeMessage.user, _) =>
            client.sendMessage(challengeMessage.channel, s"<@${challengeMessage.user}> Rejected!")
            client.removeEventListener(handler)
          // TODO: A more helpful error message would specify the possible commands you can make.
          case _ => client.sendMessage(acceptMessage.channel, s"<@${acceptMessage.user}>: I don't understand...")
        }
      }
    }
  }


  // TODO: Need to make sure stuff all happens in the one channel, otherwise it's chaos.
  def newGame(acceptMessage: Message, challengeMessage: Message): Unit = {
    // Now need to start listening to message pertinent to this game.
    // Assume a user only has one game going in a channel at any one time.
    // Now, whenever that user messages us, we assume they are talking about this game.

    // Going imperative programming until I figure this out
    var gameState = new GameState(nBoardRows, nBoardCols)

    // Print to confirm empty game with users
    client.sendMessage(acceptMessage.channel, s"<@${acceptMessage.user}>:" + gameState.boardAsString())

    // Now it's time to wait for input
    var handler = handleForHandler()
    handler = client.onMessage{ newMessage =>
      val mentionedIds = SlackUtil.extractMentionedIds(newMessage.text)

      if(mentionedIds.contains(selfId) &&
        (newMessage.user == challengeMessage.user || newMessage.user == acceptMessage.user)) {

        newMessage.text match {

          // Drop a disc/token into a column
          case Drop(_, col) =>
            client.sendMessage(newMessage.channel, s"<@${newMessage.user}>: Dropping into column $col")

            var player = playerO
            if (newMessage.user == challengeMessage.user) {
              player = playerX
            }

            // Play the move, updating the game state
            // TODO: This can be made entirely functional by recursing here.
            gameState = gameState.playMove(col.toInt, player)
            // Sends board as message to channel
            client.sendMessage(newMessage.channel, gameState.boardAsString())

            // Check for a winner and announce if so
            val winner = gameState.checkWin()
            if (winner.isDefined) {
              client.sendMessage(newMessage.channel, s"<@${newMessage.user}>: You win!")
              // End game and start listening for new one
              client.removeEventListener(handler)
              startListening()
            }

          case Stop(_) =>
            client.sendMessage(newMessage.channel, s"<@${newMessage.user}>: Forfeiting game...")
            client.removeEventListener(handler)
            startListening()
          case Reset(_) =>
            client.sendMessage(newMessage.channel, s"<@${newMessage.user}>: Forfeiting and resetting game...")
            client.removeEventListener(handler)
            newGame(acceptMessage, challengeMessage)
          case _ => client.sendMessage(newMessage.channel, s"<@${newMessage.user}>: I don't understand...")
        }

      }
    }
  }

  def handleForHandler(): ActorRef = {
    // This is weird as hell but it works at least. Creating this reference allows us to call removeEventListener
    // inside the next handler function.
    // TODO: Find a way of doing this that isn't so weird.
    client.onMessage { message =>
      // Do nothing?
    }
  }
}
