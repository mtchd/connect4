package connect4

import connect4.gamestore.DoobieTest

object Main {

  // TODO: More tests
  // TODO: Run in DMs
  def main(args: Array[String]) {

//     val token = sys.env("TOKEN")

//     SlackWrapper.startVectorening(token)

    // DiscordWrapper.startVectorening()

    // ConsoleWrapper.startVectorening()

    DoobieTest.test()

  }

}
