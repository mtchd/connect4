import Main.{nBoardCols, nBoardRows}
import akka.actor.{ActorRef, ActorSystem}
import com.typesafe.config.ConfigFactory
import slack.SlackUtil
import slack.models.Message
import slack.rtm.SlackRtmClient

import scala.concurrent.ExecutionContextExecutor

// Gives us the regex for matching message text
import CommandsRegex._

object SlackClient {

  implicit val system: ActorSystem = ActorSystem("slack")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  val client = SlackRtmClient(ConfigFactory.load().getString("secrets.slackApiKey"))
  val selfId: String = client.state.self.id

  // TODO: Create help message
  def startListening(): Unit = {

    // Currently, this starter stops listening after a single game is started. In future, it will continue listening
    // but just block out the players playing.
    var handler = handleForHandler()
    handler = client.onMessage { message =>

      val mentionedIds = SlackUtil.extractMentionedIds(message.text)

      // Checking that they have done @connect4 bot may not really be necessary. They could just say the message...
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

      acceptMessage.text match {
        case Accept(_, challengeMessage.user, _) =>
          newGame(acceptMessage, challengeMessage)
          client.removeEventListener(handler)
        case Accept(_, acceptMessage.user, _) =>
          client.sendMessage(acceptMessage.channel, s"<@${acceptMessage.user}: ${Strings.acceptSelf}")
        case Reject(_, challengeMessage.user, _) =>
          client.sendMessage(challengeMessage.channel, s"<@${challengeMessage.user}> Rejected!")
          client.removeEventListener(handler)
          startListening()
        // TODO: A more helpful error message would specify the possible commands you can make.
        case _ => client.sendMessage(acceptMessage.channel, s"<@${acceptMessage.user}>: I don't understand...")

      }
    }
  }


  // TODO: Need to make sure stuff all happens in the one channel, otherwise it's chaos.
  // TODO: Players need to take turns...
  def newGame(acceptMessage: Message, challengeMessage: Message): Unit = {
    // Now need to start listening to message pertinent to this game.
    // Assume a user only has one game going in a channel at any one time.
    // Now, whenever that user messages us, we assume they are talking about this game.

    // Create players and feed them in
    // Currently hardcoded tokens
    val challenger = new Player(challengeMessage.user, '❌')
    val defender = new Player(acceptMessage.user, '⭕')
    val gameState = new GameState(nBoardRows, nBoardCols, challenger, defender)

    // Print to confirm empty game with users
    client.sendMessage(acceptMessage.channel, s"<@${acceptMessage.user}>:" + gameState.boardAsString())

    playTurn(gameState, defendersTurn = true, acceptMessage.channel)

  }

  def playTurn(gameState: GameState, defendersTurn: Boolean, channel: String): Unit = {

    // Print board at the start of a turn
    client.sendMessage(channel, gameState.boardAsString())

    // Check for a winner and announce if so
    val winner = gameState.checkWin()
    if (winner.isDefined) {
      client.sendMessage(channel, s"<@${winner.get.slackId}>: You win!")
      // End game and start listening for new one
      startListening()
      return
    }

    // Now it's time to wait for input
    var handler = handleForHandler()
    handler = client.onMessage{ newMessage =>

      if(newMessage.user == gameState.challenger.slackId || newMessage.user == gameState.defender.slackId) {

        newMessage.text match {

          // Drop a disc/token into a column
          case Drop(_, col) =>
            client.sendMessage(newMessage.channel, s"<@${newMessage.user}>: Dropping into column $col")

            // TODO: Some non-functional stuff here, needs to be changed.
            var player = gameState.defender
            if (newMessage.user == gameState.challenger.slackId) {
              player = gameState.challenger
            }

            client.removeEventListener(handler)

            // Play the move, which gives an updated game state. Also switches whose turn it is.
            playTurn(gameState.playMove(col.toInt, player), !defendersTurn, channel)

          case Stop(_) =>
            client.sendMessage(newMessage.channel, s"<@${newMessage.user}>: Forfeiting game...")
            client.removeEventListener(handler)
            startListening()
          case Reset(_) =>
            client.sendMessage(newMessage.channel, s"<@${newMessage.user}>: Forfeiting and resetting game...")
            client.removeEventListener(handler)
            val newState = new GameState(nBoardRows, nBoardCols, gameState.challenger, gameState.defender)
            playTurn(newState, defendersTurn = true, channel)
          case _ =>
          // Do nothing, as they could have sent any message, as we are no longer disambiguating via @connect4 bot
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

  // TODO: Update to send messages to slack
  def userError(message: String): Unit = {
    println(message)
  }
}
