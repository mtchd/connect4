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

                parseMessageIntoCommand(message.content) match {
                  case Some(Command(Challenge)) => run (CreateMessage.mkContent (message.channelId, "hello"))
                  case None => run (replyMessage(message, Strings.help))
                  case _ => run (CreateMessage.mkContent (message.channelId, "Something has gone seriously wrong"))
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

  //TODO: Why does request need this _?
  def replyMessage(message: Message, reply: String): Request[_, NotUsed] = {
    CreateMessage.mkContent(message.channelId, reply)
  }

}
