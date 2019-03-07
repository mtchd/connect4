//TODO: Better naming for CommandType
//TODO: Add flags
case class Command(command: CommandType)

sealed trait CommandType
case object Challenge extends CommandType
case object Accept extends CommandType
case object Reject extends CommandType
case object Drop extends CommandType
case object Forfeit extends CommandType
case object Reset extends CommandType
case object Help extends CommandType
