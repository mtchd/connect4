import scala.util.matching.Regex

object CommandsRegex {
  // Regex to detect challenging a player.
  // In future, there will be a way of customising a player's token or the board size. This could be added here as a
  // flag.
  val Start: Regex = atUserRegex("challenge")
  // Need to specify the user you are accepting or rejecting the challenge from.
  // TODO: Regex can be merged and have just one variable changed
  val Accept: Regex = "(.*accept.*<@)(.*)(>.*)".r
  val Reject: Regex = "(.*reject.*<@)(.*)(>.*)".r
  // Available commands for running game
  val Drop: Regex = "(.*drop.*)(\\d)".r
  val Stop: Regex = "(.*forfeit.*)".r
  val Reset: Regex = "(.*reset.*)".r

  def atUserRegex(command: String): Regex = {
    s"(?i)(.*$command.*<@)(.*)(>.*)".r
  }

}
