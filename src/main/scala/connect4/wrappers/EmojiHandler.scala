package connect4.wrappers

import akka.actor.ActorSystem
import cats.effect.{ContextShift, IO, Resource}
import io.circe
import io.circe.parser
import slack.api.SlackApiClient
import io.circe.generic.auto._

import scala.io.Source

// TODO: Do I rewrite all emoji strings to fit this?
case class Emoji(short_name: String)

object EmojiHandler {

  def load(slackApiToken: String)(implicit cs: ContextShift[IO], system: ActorSystem): IO[Vector[Emoji]] =
    for {
      _ <- IO(println(s"Reading emojis..."))
      customEmojis <- getCustomEmojis(slackApiToken)
      defaultEmojis <- getDefaultEmojis
    } yield defaultEmojis ++ customEmojis

  def getCustomEmojis(slackApiToken: String)(implicit cs: ContextShift[IO], system: ActorSystem): IO[Vector[Emoji]] =
    for {
      slackApiClient <- IO(SlackApiClient(slackApiToken))
      customEmojis <- IO.fromFuture(IO(slackApiClient.listEmojis()))
    } yield customEmojis.keys.map(Emoji).toVector

  def getDefaultEmojis: IO[Vector[Emoji]] =
    Resource.fromAutoCloseable(IO(Source.fromFile("default-emojis.json"))).use { json =>
      IO.fromEither(parser.decode[Vector[Emoji]](json.mkString))
    }
}
