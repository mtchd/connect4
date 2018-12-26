import Main.{nBoardCols, nBoardRows, playerO, playerX}
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import slack.SlackUtil
import slack.models.Message
import slack.rtm.SlackRtmClient

import scala.concurrent.ExecutionContextExecutor

object SlackClient {

  implicit val system: ActorSystem = ActorSystem("slack")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  val client = SlackRtmClient(ConfigFactory.load().getString("secrets.slackApiKey"))
  val selfId: String = client.state.self.id

  def startListening(): Unit = {

    // Regex to detect challenging a player.
    // In future, there will be a way of customising a player's token or the board size. This could be added here as a
    // flag.
    val Start = "(.*challenge <@)(.*)(>)".r

    // This is weird as hell but it works at least. Creating this reference allows us to call removeEventListener
    // inside the next handler function.
    // TODO: Find a way of doing this that isn't so weird.
    var handler = client.onMessage { message =>
      // Do nothing?
    }

    // Currently, this starter stops listening after a single game is started. In future, it will continue listening
    // but just block out the players playing.
    // Perhaps a better implementation would be having one listener handle all possible interactions, redirecting
    // to particular games and applying appropriate functions.
    handler = client.onMessage { message =>
      val mentionedIds = SlackUtil.extractMentionedIds(message.text)

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

    // Now I wish I could block and wait for opponent response...

    // This is weird as hell but it works at least. Creating this reference allows us to call removeEventListener
    // inside the next handler function.
    // TODO: Find a way of doing this that isn't so weird.
    var handler = client.onMessage { message =>
      // Do nothing?
    }

    println(opponent, challengeMessage.user)

    // Need to specify the user you are accepting the challenge from. Or can make it so only one person can challenge at
    // a time?
    // Currently you can just specify anybody and it doesn't check it....
    // TODO: Regex can be merged and have just one variable changed
    // TODO: Make custom object to store regex
    val Accept = "(.*accept <@)(.*)(>)".r
    val Reject = "(.*reject <@)(.*)(>)".r

    // Stops listening after player responds to challenge
    handler = client.onMessage { acceptMessage =>

      val mentionedIds = SlackUtil.extractMentionedIds(acceptMessage.text)

      if(mentionedIds.contains(selfId) && acceptMessage.user == opponent) {

        println(challengeMessage.user, acceptMessage.text)

        acceptMessage.text match {
          case Accept(_, challengeMessage.user, _) => newGame(acceptMessage, challengeMessage)
            client.removeEventListener(handler)
          case Reject(_, challengeMessage.user, _) => client.sendMessage(challengeMessage.channel, s"<@${challengeMessage.user}> Rejected!")
            client.removeEventListener(handler)
          case _ => client.sendMessage(acceptMessage.channel, s"<@${acceptMessage.user}>: I don't understand...")
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

      // Available commands in this state, as regex
      val Drop = "(.*drop )(\\d)".r
      val Stop = "(.*forfeit)".r
      val Reset = "(.*reset)".r

      // This is weird as hell but it works at least. Creating this reference allows us to call removeEventListener
      // inside the next handler function.
      // TODO: Find a way of doing this that isn't so weird.
      var handler = client.onMessage { message =>
        // Do nothing?
      }

      // Now it's time to wait for input
      // Multiple "onMessage" things feels wrong...
      handler = client.onMessage{ newMessage =>
        val mentionedIds = SlackUtil.extractMentionedIds(newMessage.text)

        if(mentionedIds.contains(selfId) &&
          (newMessage.user == challengeMessage.user || newMessage.user == acceptMessage.user)) {

          newMessage.text match {

            case Drop(_, col) => client.sendMessage(newMessage.channel, s"<@${newMessage.user}>: Dropping into column $col")

              var player = playerO
              if (newMessage.user == challengeMessage.user) {
                player = playerX
              }

              // Play the move, updating the game state
              gameState = gameState.playMove(col.toInt, player)
              // Sends board as message to channel
              client.sendMessage(newMessage.channel, gameState.boardAsString())

              // Check for a winner and announce if so
              val winner = gameState.checkWin()
              if (winner.isDefined) {
                client.sendMessage(newMessage.channel, s"<@${newMessage.user}>: You win!")
                client.removeEventListener(handler)
              }

            case Stop(_) => client.sendMessage(newMessage.channel, s"<@${newMessage.user}>: Forfeiting game...")
              client.removeEventListener(handler)
            case Reset(_) => client.sendMessage(newMessage.channel, s"<@${newMessage.user}>: Forfeiting and resetting game...")
              client.removeEventListener(handler)
              newGame(acceptMessage, challengeMessage)
            case _ => client.sendMessage(newMessage.channel, s"<@${newMessage.user}>: I don't understand...")
          }

        }
      }
    }


  }
}
