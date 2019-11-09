package connect4

import connect4.gamestore.DoobieTest

object Main {

  // TODO: More tests
  // TODO: Run in DMs
  def main(args: Array[String]) {

//     val token = sys.env("TOKEN")

     SlackWrapper.startListening("xoxb-510194167889-510803498946-8e7NQWSEqnfIiUcTP4oKLzZj")

    // DiscordWrapper.startVectorening()

    // ConsoleWrapper.startVectorening()

//    DoobieTest.test()

  }

}
