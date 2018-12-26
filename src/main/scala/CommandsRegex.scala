import scala.util.matching.Regex

object CommandsRegex {
  // Regex to detect challenging a player.
  // In future, there will be a way of customising a player's token or the board size. This could be added here as a
  // flag.
  val Start: Regex = "(.*challenge.*<@)(.*)(>.*)".r
  // Need to specify the user you are accepting or rejecting the challenge from.
  // TODO: Regex can be merged and have just one variable changed
  // TODO: Make custom object to store regex
  val Accept: Regex = "(.*accept.*<@)(.*)(>.*)".r
  val Reject: Regex = "(.*reject.*<@)(.*)(>.*)".r
  // Available commands for running game
  val Drop: Regex = "(.*drop.*)(\\d)".r
  val Stop: Regex = "(.*forfeit.*)".r
  val Reset: Regex = "(.*reset.*)".r

}
