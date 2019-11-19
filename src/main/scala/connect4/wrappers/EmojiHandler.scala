package connect4.wrappers

import akka.actor.ActorSystem
import cats.effect.{ContextShift, IO}
import io.circe.parser
import slack.api.SlackApiClient
import io.circe.generic.auto._

import scala.io.Source

case class Emoji(short_name: String)

object EmojiHandler {

  def handle(slackApiToken: String)(implicit cs: ContextShift[IO], system: ActorSystem): IO[Unit] = {

    for {

      slackApiClient <- IO(SlackApiClient(slackApiToken))
      customEmojis <- IO.fromFuture(IO(slackApiClient.listEmojis()))
      _ <- IO(println(customEmojis))

    } yield ()

  }

  def parse(): Unit = {

    println(s"Reading...")
    val json = Source.fromFile("default-emojis.json")

    val decodeResult = parser.decode[Vector[Emoji]](json.mkString)

    json.close()

    decodeResult match {
      case Right(emojis) => emojis.foreach(println)
      case Left(error) => println(error.getMessage)
    }
  }

}
