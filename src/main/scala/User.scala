class User(val slackId: String, val challengers: List[User]) {


  /**
    * Returns new user with challenger in challenger list.
    * @param challenger User object of challenging user.
    * @return Updated user object of the challenged user.
    */
  def addChallenger(challenger: User): User = {
    // What happens if you challenge the same person twice?
    new User(slackId, challengers :+ challenger)
  }

}
