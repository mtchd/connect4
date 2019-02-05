

object Strings {

  // There's obviously a better way of storing this but I'll figure it out later
  // TODO: Better way of storing strings

  // Markers for board columns
  val colMarkers = "0⃣1⃣2⃣3⃣4⃣5⃣6⃣7⃣8⃣9⃣"
  // Help text sent with a challenge
  val newChallengeHelp = "\nThey must respond with 'accept [-flags] (optional)' or 'reject.'"
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
  // Test challenger slack ID
  val testChallengerId = "X"
  // General help, lists available commands
  // TODO: Make this just challenge and flags
  val help = s"Available commands...in unreadable regex:\n ${CommandsRegex.listCommandsAsString()}"
  val help2 = "Available commands:\n@connect4 challenge @username -flags\n"
  // Help during game
  val inGameCommands = "Available commands:\n'drop $columnNumber'\n'forfeit'\n'reset'"

}
