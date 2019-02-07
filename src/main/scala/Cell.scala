
/**
  * Single cell in the connect 4 board.
  * @param contents String representation of a players token/disc, or empty cell.
  */
// TODO: Convert to sum type, which then translates into string stored in player or strings.
case class Cell(contents: String) {

  @Override
  override def toString: String = contents

  @Override
  override def equals(that: Any): Boolean = that.toString == contents

}
