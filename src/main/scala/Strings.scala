
object Strings {

  // There's obviously a better way of storing this but I'll figure it out later
  // TODO: Better way of storing strings

  // Markers for board columns
  val ColMarkers = "0⃣1⃣2⃣3⃣4⃣5⃣6⃣7⃣8⃣9⃣"
  // Help text sent with a challenge
  val newChallengeHelp = "\nThey must respond with 'accept [-flags] (optional)' or 'reject.'"
  // Help text when failing to respond to a challenge
  val challengeHelp = "You will need to answer the challenge with 'accept' or 'reject"
  // Token for empty space on the board.
  val EmptySpace = ":white_circle:"
  // Default token for a challenger
  val ChallengerToken = ":red_circle:"
  // Default token for a defender
  val DefenderToken = ":large_blue_circle:"
  // Turns winning 4 tokens into these
  val WinningToken = ":medal:"
  // Test challenger slack ID
  val testChallengerId = "X"
  // General help, lists available commands
  val help = "Available commands:\n@connect4 challenge @username -flags\n"
  // Help during game
  val inGameCommands = "Available commands:\n'drop $columnNumber'\n'forfeit'\n'reset'"
  // When accepting or rejecting when not challenged by anyone
  val FailedAcceptOrReject = "You have not been challenged here, although you do seem mentally challenged."

}
