package connect4

import cats.effect.{ContextShift, IO}
import connect4.wrappers.SlackWrapper

import scala.concurrent.ExecutionContext.Implicits.global

object Main {

  implicit val cs: ContextShift[IO] = IO.contextShift(global)

  // TODO: More tests
  def main(args: Array[String]) {

    val slackRtmToken = sys.env("SLACK_RTM_TOKEN")
    val dbPassword = sys.env("DB_PASS")
    val slackApiToken = sys.env("SLACK_API_TOKEN")

    SlackWrapper.startListening(slackRtmToken, dbPassword, slackApiToken)

  }

}
