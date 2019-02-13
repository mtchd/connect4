import scala.util.matching.Regex

//TODO: Look into Parser Combinators
object CommandsRegex {
  // Regex for challenging and accepting/rejecting a game
  val Challenge: Regex = atUserRegex("challenge")
  val Accept: Regex = "(?i)(.*accept)(.*)".r
  val Reject: Regex = simpleRegex("reject")
  // Available commands for running game
  val Drop: Regex = "(?i)(.*)(\\d)(.*)".r
  val Forfeit: Regex = simpleRegex("forfeit")
  val Reset: Regex = simpleRegex("reset")
  val Help: Regex = simpleRegex("help")
  // Flags
  val TokenFlag: Regex = "(?i)(.*-token.*=.*)(:.*:)(.*)".r

  def atUserRegex(command: String): Regex = {
    // Note that using the (?i) flag, i.e. case insensitive flag, we take a small performance hit,
    // source: https://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html#special
    s"(?i)(.*$command.*<@)(.*)(>.*)".r
  }

  // This is a bit loose, they just have to mention the word anywhere in what they are saying and it will accept.
  // However given the tendency of users to capitalise (esp. on phone) and add accidental spaces, this seems best.
  // Especially given the command will always be in a thread for the game
  def simpleRegex(command: String): Regex = {
    s"(?i)(.*$command.*)".r
  }

  // Lists commands as string. Currently unused but available for use if needed.
  def listCommandsAsString(): String = {
    val fields = CommandsRegex.getClass.getDeclaredFields

    var list: List[AnyRef] = List()

    fields.foreach { f =>
      f.setAccessible(true)
      list = list :+ f.get(CommandsRegex)
    }

    list.mkString("\n")
  }

}
