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

    //TODO: Investigate using a ListBuffer instead of a list, seeing as we are going mutable.
    var gameInstances: List[GameInstance] = List.empty

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
                val (newGameInstances, reply) = handleMessage(message, gameInstances)
                gameInstances = newGameInstances
                run (replyMessage(message, reply))
              }
            } yield ()
          case _ => client.sourceRequesterRunner.unit
        } }
      }
      client.login()
    }
  }

  def handleMessage(message: Message, gameInstances: List[GameInstance]): (List[GameInstance], String) = {
    message.content match {
      // Might not be in commandHandler's scope
      case CommandsRegex.Challenge(_, opponentId, _) => CommandHandler.challenge(gameInstances, opponentId, message.authorId.toString)
      case CommandsRegex.Accept(_, _) =>
        // We assume a player is only in one game at once. Discord does not have threading like
        // slack, so we'll need a new alternative to disambiguate what game they are referring to.

        // Passing side effects to command handler?
        // Could make a ID type known as DiscordId that handles this, makes it less side effecty
        CommandHandler.accept(gameInstances, message.authorId.toString)
      // TODO: Is using toString okay?
      case CommandsRegex.Drop(_, col, _) => CommandHandler.drop(col.toInt, gameInstances, message.authorId.toString)
      case CommandsRegex.Forfeit(_) => CommandHandler.forfeit(gameInstances, message.authorId.toString)
      case CommandsRegex.Reject(_) => CommandHandler.reject(gameInstances, message.authorId.toString)
      case _ => (gameInstances, Strings.Help)
    }
  }

  // TODO: Why does request need _ and NotUsed?
  // Formats our reply message nicely, we can @ the user here
  def replyMessage(message: Message, reply: String): Request[_, NotUsed] = {
    CreateMessage.mkContent(message.channelId, reply)
  }

}
