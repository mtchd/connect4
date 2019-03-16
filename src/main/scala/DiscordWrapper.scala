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
                val (newGameInstances, reply) = CommandHandler.interpret(message.content, message.authorId.toString, gameInstances)
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

  // TODO: Why does request need _ and NotUsed?
  // Formats our reply message nicely, we can @ the user here
  private def replyMessage(message: Message, reply: String): Request[_, NotUsed] = {
    CreateMessage.mkContent(message.channelId, reply)
  }

}
