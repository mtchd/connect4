import connect4.CommandsRegex
import org.scalatest.FunSuite

class CommandsRegexTest extends FunSuite {

  test("testDrop") {

    val sillyString = "10"

    val colNum = sillyString match {
      case CommandsRegex.Drop(col) => col.toInt
    }

    assert(colNum == 10)

  }

}
