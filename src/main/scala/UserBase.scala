/**
  * Static, semi-mutable, object for storing all players and relevant information.
  */
object UserBase {

  // Overwritten by new one each time we change it
  // One day, I dream, I will have this program totally immutable. But this will have to be the one exception.
  var userList: List[User] = List()

  def getUser(slackId: String): User = {

    val user = userList.find( user => user.slackId == slackId )

    user.getOrElse {
      // Need to make new user and attach to list
      val newUser = new User(slackId, List())
      userList = userList :+ newUser
      newUser
    }

  }

  def updateUser(updatedUser: User): Unit = {

    // I think this assignment might be redundant, syntactically. Test when able.
    userList = userList.map( user => if (user.slackId == updatedUser.slackId) updatedUser else user )

  }

}
