import scala.util.matching.Regex

//TODO: There's got to be a better way to store this.
object CommandsRegex {
  // Regex to detect challenging a player.
  // In future, there will be a way of customising a player's token or the board size. This could be added here as a
  // flag.
  val Start: Regex = atUserRegex("challenge")
  // Need to specify the user you are accepting or rejecting the challenge from. Or do you? Could just be only one
  // challenge on each user per channel.
  val Accept: Regex = simpleRegex("accept")
  val Reject: Regex = simpleRegex("reject")
  // Available commands for running game
  val Drop: Regex = "(?i)(.*drop.*)(\\d)".r
  val Stop: Regex = simpleRegex("forfeit")
  val Reset: Regex = simpleRegex("reset")
  val Help: Regex = simpleRegex("help")

  def atUserRegex(command: String): Regex = {
    // Note that using the (?i) flag, i.e. case insensitive flag, we take a small performance hit,
    // source: https://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html#special
    s"(?i)(.*$command.*<@)(.*)(>.*)".r
  }

  // This is a bit loose, they just have to mention the word anywhere in what they are saying and it will accept.
  // However given the tendency of users to capitalise (esp. on phone) and add accidental spaces, this seems best.
  def simpleRegex(command: String): Regex = {
    s"(?i)(.*$command.*)".r
  }

}
