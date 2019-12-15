package connect4.wrappers

import akka.actor.ActorSystem
import cats.effect.{ContextShift, IO, Resource}
import connect4.commands.CommandsRegex
import connect4.wrappers.EmojiHandler.convertEmoji
import io.circe.parser
import slack.api.SlackApiClient
import io.circe.generic.auto._
import scala.io.Source

// TODO: Do I rewrite all emoji strings to fit this?
case class Emoji(short_name: String) {
  def coloned: String = {
    ":" + short_name + ":"
  }
}

case class RawEmoji(short_name: String, has_skin: Boolean, is_skin: Boolean)

case class EmojiHandler(invariantEmojis: Set[Emoji], skinnableEmojis: Set[Emoji], skinEmojis: Set[Emoji]) {

  def validateAndExtractEmoji(input: String, default: String): String = {

    // TODO: Feels like the input should be sanitised earlier - like in the regex?
    val extractedEmojis: Vector[String] = input match {
      case CommandsRegex.Emoji(_*) => CommandsRegex.Emoji2.findAllIn(input).toVector
      case _ => Vector.empty
    }

    extractedEmojis.map(convertEmoji) match {
      case Vector(emoji) => if (invariantEmojis.contains(emoji)) emoji.coloned else default
      case Vector(emoji, skin) => if (skinnableEmojis.contains(emoji) && skinEmojis.contains(skin)) emoji.coloned + skin.coloned else default
      case _ => default
    }

  }
}

object EmojiHandler {

  def load(slackApiToken: String)(implicit cs: ContextShift[IO], system: ActorSystem): IO[EmojiHandler] =
    for {
      _ <- IO(println("Reading emojis..."))
      customEmojis <- getCustomEmojis(slackApiToken)
      defaultEmojis <- getDefaultEmojis
      (invariant, skinnable, skin) = sortEmojis(defaultEmojis)
    } yield EmojiHandler(invariant ++ customEmojis, skinnable, skin)

  def getCustomEmojis(slackApiToken: String)(implicit cs: ContextShift[IO], system: ActorSystem): IO[Set[Emoji]] =
    for {
      slackApiClient <- IO(SlackApiClient(slackApiToken))
      customEmojis <- IO.fromFuture(IO(slackApiClient.listEmojis()))
    } yield customEmojis.keys.map(Emoji).toSet

  def getDefaultEmojis: IO[Vector[RawEmoji]] =
    Resource.fromAutoCloseable(IO(Source.fromFile("default-emojis.json"))).use { json =>
      IO.fromEither(parser.decode[Vector[RawEmoji]](json.mkString))
    }

  def sortEmojis(rawEmojis: Vector[RawEmoji]): (Set[Emoji], Set[Emoji], Set[Emoji]) = {
    // Create three sets of Emojis to fuck with
    // Loop through, just do it mutable cuz I don't know better
    var (invariant, skinnable, skin) = (Set.empty[Emoji], Set.empty[Emoji], Set.empty[Emoji])
    rawEmojis.foreach {
      // Should crash if we find anything else
      case RawEmoji(short_name, true, false) => skinnable = skinnable + Emoji(short_name)
      case RawEmoji(short_name, false, true) => skin = skin + Emoji(short_name)
      case RawEmoji(short_name, false, false) => invariant = invariant + Emoji(short_name)
    }
    (invariant, skinnable, skin)
  }

  def convertEmoji(input: String): Emoji = Emoji(input.replaceAll(":",""))
}


