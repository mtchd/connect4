object UserBase {

  // Overwritten by new one each time we change it
  var userList: List[User] = List()

  def getUser(slackId: String): User = {

    val user = userList.find( user => user.slackId == slackId )

    user.getOrElse {
      // Need to make new user and attach to list
      val newUser = new User(slackId)
      userList = userList :+ newUser
      newUser
    }

  }

}
