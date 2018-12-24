import akka.actor.ActorSystem
import slack.SlackUtil
import slack.models.Message

import scala.annotation.switch
import scala.io.StdIn
import slack.rtm.SlackRtmClient

import scala.concurrent.ExecutionContextExecutor // Async

object Main {

  val boardCols = 6
  val boardRows = 7

  val playerX = new Player('❌')
  val playerO = new Player('O')

  val emptySpace = "⚪"
  val emptySpaceC = '⚪'

  // Slack client stuff
  //...yep
  val token = ""
  implicit val system: ActorSystem = ActorSystem("slack")
  implicit val ec: ExecutionContextExecutor = system.dispatcher
  val client = SlackRtmClient(token)
  val selfId: String = client.state.self.id


  // TODO: Classes for more things, such as cells, then have rows/cols as List[Cell}
  // TODO: Tests
  def main(args: Array[String]) {

    // Most things are hardcoded at this stage of the project, to be cleaned up.
    println("Welcome.")
    // TODO: Send a slack message to say I'm online?

    // Currently just detects whatever is at the back of the message
    val Start = "(.*start)".r
    val Stop = "(.*forfeit)".r
    val Reset = "(.*reset)".r

    // Maybe add a game to be attached to a challenging player?
    // This concurrent stuff is hard.

    client.onMessage { message =>
      val mentionedIds = SlackUtil.extractMentionedIds(message.text)

      if(mentionedIds.contains(selfId)) {

        message.text match {
            // user will need to @ another user to specify who they are playing against?
            // There will need to be support for multiple users playing each other simultaneously in future
          case Start(_) => newGame(message) // Needs to start ignoring players invovled.
          case Stop(_) => client.sendMessage(message.channel, s"<@${message.user}>: Forfeiting game...")
          case Reset(_) => client.sendMessage(message.channel, s"<@${message.user}>: Forfeiting and resetting game...")
          case _ => client.sendMessage(message.channel, s"<@${message.user}>: I don't understand...")
        }

      }
    }

    gameLoop(new GameState(boardRows, boardCols))

  }

  def newGame(message: Message): Unit = {

    client.sendMessage(message.channel, s"<@${message.user}>: Starting game...")

    // Now need to start listening to message pertinent to this game.
    // Assume a user only has one game going in a channel at any one time.
    // Now, whenever that user messages us, we assume they are talking about this game.

    // Going imperative programming until I figure this out
    var gameState = new GameState(boardRows, boardCols)

    // Print to confirm empty game with users
    client.sendMessage(message.channel, s"<@${message.user}>:" + gameState.boardAsString())

    val Drop = "(.*drop )(\\d)".r

    // Now it's time to wait for input
    // Multiple "onMessage" things feels wrong...
    client.onMessage{ newMessage =>
      val mentionedIds = SlackUtil.extractMentionedIds(newMessage.text)

      if(mentionedIds.contains(selfId) && newMessage.user == message.user) {

        newMessage.text match {

          case Drop(_, col) => client.sendMessage(message.channel, s"<@${message.user}>: Dropping into column $col")
            gameState = gameState.playMove(col.toInt, playerX)
            client.sendMessage(message.channel, gameState.boardAsString())
            if (gameState.checkWin().isDefined)
              client.sendMessage(message.channel, s"<@${message.user}>: You win!")

          case _ => client.sendMessage(message.channel, s"<@${message.user}>: I don't understand...")
        }

      }
    }
  }

  // Could use error codes instead of manually entering error.
  def userError(message: String): Unit = {
    // Needs to tell user their input is an error, and return them to start of their turn.
    println(message)
  }

  def playTurn(gameState: GameState, player: Player): GameState ={

    val col = StdIn.readLine("Enter column number:").toInt

    // Check col within bounds (0 to number of columns take 1)
    if (col >= 0 && col < boardCols) {
      gameState.playMove(col, player)
    }
    else {
      userError("Column out of bounds. Try again.")
      playTurn(gameState, player)
    }

  }

  def gameLoop(gameState: GameState): Option[Player] = {

    gameState.printBoard()

    // Check if someone has won, and finish game if so.
    val winner = gameState.checkWin()
    if (winner.isDefined) {
      val winningPlayer = winner.get
      println(winningPlayer.token + " wins!")
      // Finish game
      return Some(winningPlayer)
    }

    val command = parseInput(StdIn.readLine("Enter Command:"))

    (command: @switch) match {
      case 0 => userError("Invalid input.") ; gameLoop(gameState)
      // Ideally, players are a proper object and not just a character, but that is to come.
      case 1 => gameLoop(playTurn(gameState, playerX ))
      case 2 => gameLoop(playTurn(gameState, playerO))
      case 3 => gameLoop(new GameState(boardRows, boardCols))
      // Return none to signify draw.
      case 4 => None
    }

  }

  /**
    * Takes command line input and returns a code corresponding to the command.
    * This uncouples command inputs, so it can be recomposed later for Slack.
    * @param input Command entered by user.
    * @return Code indicating what to execute.
    */
  def parseInput(input: String):  Int = {

    (input: @switch) match {
      case "X" => 1
      case "O" => 2
      case "reset" => 3
      case "exit" => 4
      case _ => 0
    }

  }
}