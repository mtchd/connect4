import java.lang.reflect.Field

object Strings {

  // There's obviously a better way of storing this but I'll figure it out later
  // TODO: Better way of storing strings

  // Markers for board columns
  val colMarkers = "0⃣1⃣2⃣3⃣4⃣5⃣6⃣7⃣8⃣9⃣"
  // Help text sent with a challenge
  val newChallengeHelp = "\nThey must respond with 'accept' or 'reject.'"
  // Help text when failing to respond to a challenge
  val challengeHelp = "You will need to answer the challenge with 'accept' or 'reject"
  // Token for empty space on the board.
  val emptySpace = ":white_circle:"
  // Default token for a challenger
  val challengerToken = ":red_circle:"
  // Default token for a defender
  val defenderToken = ":large_blue_circle:"
  // Turns winning 4 tokens into these
  val winningToken = ":medal:"
  // General help, lists available commands
  val help = s"Available commands...in unreadable regex:\n ${listCommandsAsString()}"

  def listCommandsAsString():String = {
    val fields = CommandsRegex.getClass.getDeclaredFields

    var list: List[AnyRef] = List()

    fields.foreach { f =>
      f.setAccessible(true)
      list = list :+ f.get(CommandsRegex)
    }

    list.mkString("\n")
  }

}
