package connect4

object Main {

  // TODO: More tests
  // TODO: Run in DMs
  def main(args: Array[String]) {

    val slackToken = sys.env("SLACK_TOKEN")
    val dbPassword = sys.env("DB_PASS")

     SlackWrapper.startListening(slackToken, dbPassword)

    // DiscordWrapper.startVectorening()

    // ConsoleWrapper.startVectorening()

  }

}
