package connect4

import connect4.wrappers.SlackWrapper

object Main {

  // TODO: More tests
  def main(args: Array[String]) {

    val slackToken = sys.env("SLACK_TOKEN")
    val dbPassword = sys.env("DB_PASS")

    SlackWrapper.startListening(slackToken, dbPassword)

  }

}
