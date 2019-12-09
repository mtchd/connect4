package connect4.wrappers

import akka.actor.ActorSystem
import cats.effect.{ContextShift, IO, Resource}
import connect4.commands.CommandsRegex
import connect4.wrappers.EmojiData.{Invariant, Skin, Skinnable}
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
      _ <- IO(println("Reading emojis..."))
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

  def validateAndExtractEmoji(emojis: Vector[Emoji], input: String, default: String): String = {

    // TODO: Feels like the input should be sanitised earlier - like in the regex?
    val extractedEmojis: Vector[String] = input match {
      case CommandsRegex.Emoji(_*) => CommandsRegex.Emoji2.findAllIn(input).toVector
      case _ => Vector.empty
    }

    // TODO: Finish this
    extractedEmojis match {
      case Vector(emoji) => if (emojis.contains(convertEmoji(input))) input else default
      case Vector(emoji, skin) => default // Validate the first is skinnable, 2nd is skin
      case _ => default
    }

  }

  def convertEmoji(input: String): Emoji = Emoji(input.replaceAll(":",""))
}

sealed trait EmojiData
object EmojiData {
  case class Invariant(short_name: String) extends EmojiData
  case class Skinnable(short_name: String) extends EmojiData
  case class Skin(short_name: String) extends EmojiData
}

sealed trait Emoji2 {
  case class Normal(invariant: Invariant) extends Emoji2
  case class Skinned(skinnable: Skinnable, skin: Skin) extends Emoji2
}
