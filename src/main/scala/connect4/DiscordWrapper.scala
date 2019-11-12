package connect4

import akka.NotUsed
import cats.Monad
import com.typesafe.config.ConfigFactory
import net.katsstuff.ackcord.data.{Message, UserId}
import net.katsstuff.ackcord.http.rest.CreateMessage
import net.katsstuff.ackcord._

object DiscordWrapper {

  // Side effects be here, yarr

  def startListening(): Unit = {

    //TODO: Investigate using a VectorBuffer instead of a Vector, seeing as we are going mutable.
    var gameInstance: Option[GameInstance] = None

    val token = ConfigFactory.load().getString("secrets.discordApiKey")
    val clientSettings = ClientSettings(token)
    import clientSettings.executionContext
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
                // Check that statement begins with @connect4
                val (newGameInstances, reply) = CommandInterpreter.interpret(message.content, message.authorId.toString, gameInstance)
                gameInstance = newGameInstances
                reply match {
                  case Some(text) => run (replyMessage(message, text))
                  case None => client.sourceRequesterRunner.unit
                }
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
