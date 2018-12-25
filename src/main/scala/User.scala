/**
  * A user.
  * @param slackId Slack ID of user.
  * @param challengers List of Slack IDs of users who have challenged this user.
  */
class User(val slackId: String, val challengers: List[String]) {


  /**
    * Returns new user with challenger in challenger list.
    * @param challengerId Slack ID of challenging user.
    * @return Updated user object of the challenged user.
    */
  def addChallenger(challengerId: String): User = {
    // What happens if you challenge the same person twice?
    new User(slackId, challengers :+ challengerId)
  }

  // This add and remove code could be redundant...if there were some way to just update a part of the user object
  // (i.e challengers list) and return the new user.
  /**
    * Removes rejected challenger from challenger list.
    * @param rejectedChallengerId Slack ID of Challenger to remove from the list
    * @return Updated user without ya boi in the list
    */
  def removeChallenger(rejectedChallengerId: String): User = {
    new User(slackId, challengers.filter(_ != rejectedChallengerId))
  }

}
