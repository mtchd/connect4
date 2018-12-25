class User(val slackId: String) {

  var challengers: Seq[User] => List[User] = List[User]

}
