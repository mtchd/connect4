import Main._
import akka.actor.ActorSystem
import com.typesafe.config.ConfigFactory
import slack.SlackUtil
import slack.models.Message
import slack.rtm.SlackRtmClient

import scala.concurrent.ExecutionContextExecutor

import CommandsRegex._

object SlackClient {

  // Slack client stuff
  implicit val system: ActorSystem = ActorSystem("slack")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  val client = SlackRtmClient(ConfigFactory.load().getString("secrets.slackApiKey"))
  val selfId: String = client.state.self.id

  def startListening(): Unit = {

    client.onMessage { message =>

      val mentionedIds = SlackUtil.extractMentionedIds(message.text)

      if(mentionedIds.contains(selfId)) {

        // Match to command from CommandsRegex
        message.text match {
          // Hmm, we could try player.becomeChallenged and player.games and stuff like that
          case Challenge(_, opponentId) => challenge(message, opponentId)
          case Accept(_, message.user) => newGame(message)
          case Reject(_, message.user) => reject(message)
          case Stop(_) => client.sendMessage(message.channel, s"<@${message.user}>: Forfeiting game...")
          case Reset(_) => client.sendMessage(message.channel, s"<@${message.user}>: Forfeiting and resetting game...")
          case Drop(_, col) => drop(message, col.toInt)

          case _ => client.sendMessage(message.channel, s"<@${message.user}>: I don't understand...")
        }
      }
    }
  }

  /**
    * Challenges a player to a match.
    * @param message Challenging message that initiated game.
    * @param challengedId Slack id of opponent challenged.
    */
  def challenge(message: Message, challengedId: String): Unit = {

    // TODO: Give these messages buttons for users to press.
    val instructions = "\nThey must respond with 'accept' or 'reject.'"
    client.sendMessage(message.channel, s"<@${message.user}>: Challenging $challengedId...$instructions")

    // Now we need to make that player have the "challenged" tag.
    // First, we need to get the players attached to that ID
    val challenged = UserBase.getUser(challengedId)
    val challenger = UserBase.getUser(message.user)

    // So now the challenged user should have this guy as his challenger.
    UserBase.updateUser(challenger.addChallenger(challenged))

  }

  // Currently you reject yourself lol
  def reject(message: Message): Unit = {
    client.sendMessage(message.channel, s"<@${message.user}> Rejected!")
  }

  def newGame(acceptMessage: Message): Unit = {
    // Going imperative programming until I figure this out
    val gameState = new GameState(nBoardRows, nBoardCols)

    // Print to confirm empty game with users
    client.sendMessage(acceptMessage.channel, s"<@${acceptMessage.user}>:" + gameState.boardAsString())

  }

  def drop(message: Message, col: Int): Unit = {

    client.sendMessage(message.channel, s"<@${message.user}>: Dropping into column $col")
    /*
    var player = playerO
    if (newMessage.user == message.user) {
      player = playerX
    }
    gameState = gameState.playMove(col.toInt, player)
    client.sendMessage(message.channel, gameState.boardAsString())
    if (gameState.checkWin().isDefined)
      client.sendMessage(message.channel, s"<@${message.user}>: You win!")
      */
  }
}
