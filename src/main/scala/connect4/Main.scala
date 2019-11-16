package connect4

import connect4.wrappers.SlackWrapper

object Main {

  // TODO: More tests
  def main(args: Array[String]) {

    val slackRtmToken = sys.env("SLACK_RTM_TOKEN")
    val dbPassword = sys.env("DB_PASS")
    val slackApiToken = sys.env("SLACK_API_TOKEN")

    SlackWrapper.startListening(slackRtmToken, dbPassword, slackApiToken)

  }

}
