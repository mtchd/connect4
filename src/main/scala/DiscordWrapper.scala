import akka.{Done, NotUsed}
import com.typesafe.config.ConfigFactory
import net.katsstuff.ackcord._
import net.katsstuff.ackcord.data._
import net.katsstuff.ackcord.syntax._
import net.katsstuff.ackcord.commands._
import net.katsstuff.ackcord.http.rest.CreateMessage
import cats.{Id, Monad}

object DiscordWrapper {

  // Side effects be here, yarr
  val GeneralCommands = "!"
  private val token = ConfigFactory.load().getString("secrets.discordApiKey")
  private val settings = ClientSettings(token, commandSettings = CommandSettings(prefixes = Set(GeneralCommands), needsMention = true))
  import settings.executionContext

  def startListening(): Unit = {

    // Make a case class instead of using a tuple
    // Maybe make a case class with two players and a gameState
    var gameInstances = List.empty
    var challengePairs = List.empty

    val clientSettings = ClientSettings(token)

    val futureClient = clientSettings.createClient()

    futureClient.foreach { client =>

      client.onEvent {
        client.withCache[SourceRequest, APIMessage] { implicit c => {

          case APIMessage.Ready(_) => Monad[SourceRequest].pure(println("Now ready"))

          case APIMessage.MessageCreate(message, _) =>

            import client.sourceRequesterRunner._

            for {
              user <- liftOptionT(UserId(message.authorId).resolve)
              // TODO: Fix if statement hack
              _ <- if (user.bot.getOrElse(false)) {
                client.sourceRequesterRunner.unit
              } else {

                message.content match {
                  case CommandsRegex.Challenge(_, opponentId, _) =>

                    // Kick off a new game instance?
                    println(message.content)

                    // We create a whole new game, even though they could be rejected...
                    // An alternative to this is creating a pair of players, saving that to a list, then creating the
                    // game instance apon accept. But I think this is cleaner code, although less efficient.
                    val (gameInstance, reply) =
                      CommandHandler.challenge(opponentId, message.authorId.toString)

                    gameInstances = gameInstances :+ gameInstance
                    run (replyMessage(message, reply))

                  case CommandsRegex.Accept(_,_) =>
                    // We assume a player is only in one game at once. Discord does not have threading like
                    // slack, so we'll need a new alternative to disambiguate what game they are referring to.

                    // Now, do a search of pairs for the first one that has the accepting player as the defender.

                    if (gameInstances.exists( gameInstance =>
                      gameInstance.defender.id == message.authorId.toString
                        && gameInstance.phase == ChallengingPhase)) {

                      // Okay, move game to "playing" phase
                    }

                    gameInstances.map(gameInstance =>

                      if (gameInstance.defender.id == message.authorId.toString
                        && gameInstance.phase == ChallengingPhase) {
                        gameInstance.changePhase(PlayingPhase)
                      } else {

                      }
                    )

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




                    // Make playerPair class that you can 2 tuple with gameState, then search games for that.
                    // This keeps player in the side effect layer, as it's id and token are both side effects.
                    // The 'role' is then passed down, which the side effect free part.
                    run (replyMessage(message, CommandHandler.accept()))

                  case CommandsRegex.Drop(_, col, _) =>
                    // Find the players responsible and pass the command down. (So command with two users?
                    // How do we know what game the users are associated with?
                  case _ =>
                    run (replyMessage(message, Strings.help))
                }

              }
            } yield ()

          case _ => client.sourceRequesterRunner.unit
        } }
      }

      client.login()
    }
  }

  def parseMessageIntoCommand(message: String): Option[Command] = {
    message match {
      case CommandsRegex.Challenge(_, opponent, _) => Some(Command(Challenge))
      case _ => None
    }
  }

  // TODO: Why does request need _ ?
  // Formats our reply message nicely, we can @ the user here
  def replyMessage(message: Message, reply: String): Request[_, NotUsed] = {
    CreateMessage.mkContent(message.channelId, reply)
  }

}
