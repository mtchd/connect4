import scala.util.matching.Regex

object CommandsRegex {

  // Regex to detect challenging a player.
  // In future, there will be a way of customising a player's token or the board size. This could be added here as a
  // flag.
  val Challenge: Regex = "(.*challenge )(<@.*>)".r
  val Accept: Regex = "(.*accept )(<@.*>)".r
  val Reject: Regex = "(.*reject )(<@.*>)".r
  val Drop: Regex = "(.*drop )(\\d)".r
  val Stop: Regex = "(.*forfeit)".r
  val Reset: Regex = "(.*reset)".r
}
