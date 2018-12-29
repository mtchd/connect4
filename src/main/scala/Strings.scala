import java.lang.reflect.Field

object Strings {

  // There's obviously a better way of storing this but I'll figure it out later
  // TODO: Better way of storing strings

  val colMarkers = "0⃣1⃣2⃣3⃣4⃣5⃣6⃣7⃣8⃣9⃣"
  val challengeHelp = "You will need to answer the challenge with 'accept' or 'reject"
  val challengerToken = ":red_circle:"
  val defenderToken = ":large_blue_circle:"
  val instructions = "\nThey must respond with 'accept' or 'reject.'"
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