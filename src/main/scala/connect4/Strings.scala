package connect4

import connect4.gamestore.ScoreStoreRow

object Strings {

  // TODO: Better way of storing strings

  // Help text sent with a challenge
  val NewChallengeHelp = "\nYou can respond with:\naccept\nreject\naccept :emoji:"
  // Help text when failing to respond to a challenge
  val ChallengeHelp = "You will need to answer the challenge with 'accept' or 'reject"

  // Slack / Discord Tokens
  // Token for empty space on the board.
  val EmptySpace = ":white_circle:"
  // Default token for a challenger
  val ChallengerToken = ":red_circle:"
  // Default token for a defender
  val DefenderToken = ":large_blue_circle:"
  // Turns winning 4 tokens into these
  val WinningToken = ":medal:"
  // Markers for board columns
  val ColMarkers = "0⃣1⃣2⃣3⃣4⃣5⃣6⃣7⃣8⃣9⃣"

  // Console Tokens
  // Token for empty space on the board.
  val ConsoleEmptySpace = "_"
  // Default token for a challenger
  val ConsoleChallengerToken = "C"
  // Default token for a defender
  val ConsoleDefenderToken = "D"
  // Turns winning 4 tokens into these
  val ConsoleWinningToken = "W"
  val ConsoleColMarkers = "0123456789"

  // Test challenger slack ID
  val TestChallengerId = "X"
  // General help, Vectors available commands
  val Help = "To challenge a player, use:" +
    "\nchallenge @username :emoji:, where :emoji: is your token for the game" +
    "\nYou can add/change a token with this flag 'token :your-token-here:'" +
    "\nWhen in a game, simply type the number of the column you want to drop into." +
    "\nYou can also type 'forfeit' to give up."
  // Help during game
  val InGameCommands = "Available commands:\ncolumn number (e.g. '1')\nforfeit"
  // When accepting or rejecting when not challenged by anyone
  val FailedAcceptOrReject = "You have not been challenged here!"
  // When a play enters a drop command but isn't in a game
  val FailedDrop = "You are not in a game here!"
  // When a player enters a drop command but it's not their turn
  val WrongTurn = "It's not your turn!"
  // When player tries to drop in column out of bounds
  val OutOfBounds = "Column is out of bounds!"
  // Column is full
  val ColFull = "Column is full!"

  val Forfeit = "You gave up. Game over, man."

  val FailedForfeit = "You're not in a game, don't go giving up already!"

  val Reject = "You rejected the challenge!"

  val FailedRenderBoard = "No game playing with this pair."

  val AlreadyGame = "There already is a game in this context."

  val NotInGame = "You don't seem to be associated with a game here."

  val Win = "You win!\n"

  val HaventPlayed = "You haven't played a game yet!"

  def dropSuccess(col: Int): String = s"Dropped into column $col"

  def tokenChange(token: String): String = s"You changed your token to $token"

  def reportScore(score: ScoreStoreRow) = s"<@${score.playerId}> has ${score.wins} wins and ${score.losses} losses."

  def atUser(userId: String, reply: String) = s"<@${userId}>: $reply"

}
