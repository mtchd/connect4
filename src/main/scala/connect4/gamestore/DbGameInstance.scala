package connect4.gamestore

import connect4.Cell

case class DbGameInstance(
                           challengerId: String,
                           defenderId: String,
                           challengerToken: String,
                           defenderToken: String,
                           lastMovePlayer: String,
                           lastMoveCol: Int,
                           lastMoveRow: Int,


                         )
