package connect4.wrappers

import akka.actor.ActorSystem
import cats.effect.IO
import cats.implicits._
import connect4.Strings
import connect4.commands.{Challenge, CommandHandler, CommandInterpreter, GameContext, GameContextCommand, Help, NoContext, NoReply, ScoreContext}
import connect4.game.{Finished, GameInstance, Ranked, UnRanked}
import connect4.gamestore.{GameStoreRow, RDSGameStore, ScoreStoreRow}
import slack.api.SlackApiClient
import slack.models.Message
import slack.rtm.SlackRtmClient

import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt

object SlackWrapper {

  implicit val system: ActorSystem = ActorSystem("slack"/* config.getConfig("akka")*/)
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  /**
    * Start point of the program, handles all incoming messages in channels the bot is present in. Side effects be
    * here, but only here.
    */
  def startListening(slackToken: String, password: String): Unit = {

    val rtmClient = SlackRtmClient(slackToken, SlackApiClient.defaultSlackApiBaseUri, 20.seconds)
    val gameStore = RDSGameStore(password)

    gameStore.setupGameStore().unsafeRunSync()
    gameStore.setupScoreStore().unsafeRunSync()

    println("Now listening to Slack...")

    rtmClient.onMessage { message =>

      val pro: IO[Any] = CommandInterpreter.bigBadInterpret(message.text) match {
        case NoReply => IO(Unit)
        case NoContext(command) => {
          val thread = message.thread_ts.getOrElse(message.ts)
          val replyText = CommandInterpreter.interpretNoContextCommand(command)
          IO(rtmClient.sendMessage(message.channel, s"<@${message.user}>: $replyText", Some(thread)))
        }
        case GameContext(command) => {
          val thread = message.thread_ts.getOrElse(message.ts)

          for {
            maybeGameRow <- gameStore.get(thread)
            maybeGameInstance = RDSGameStore.convertGame(maybeGameRow)

            _ <- maybeGameInstance match {
              case Some(gameInstance) => handleGame(gameInstance, command, message.user, rtmClient, message, gameStore, thread) // Continue
              case None => {
                command match {
                  case Challenge(opponentId, flags) => handleChallenge(message.user, opponentId, flags, rtmClient, message, gameStore, thread)
                  case _ => {
                    val replyText = Strings.NotInGame
                    IO(rtmClient.sendMessage(message.channel, s"<@${message.user}>: $replyText", Some(thread)))
                  }
                }
              }
            }

          } yield ()

        }
        case ScoreContext(command) => {
          val thread = message.thread_ts.getOrElse(message.ts)
          val replyText = CommandInterpreter.interpretScoreContextCommand(command)
          IO(rtmClient.sendMessage(message.channel, s"<@${message.user}>: $replyText", Some(thread)))
        }
      }

      val messageResponseProgram = for {
        // Use information in message to *maybe* query database for relevant thread
        thread <- IO(message.thread_ts.getOrElse(message.ts))

        // ThreadId => IO[Option[GameInstance]]
        maybeGameRow <- gameStore.get(thread)
        maybeGameInstance = RDSGameStore.convertGame(maybeGameRow)

        (newMaybeGameInstance, reply) = CommandInterpreter.interpret(message.text, message.user, maybeGameInstance)

        _ <- reply.traverse_ { replyText =>
          IO(rtmClient.sendMessage(message.channel, s"<@${message.user}>: $replyText", Some(thread)))
        }

        _ <- putGameInstance(newMaybeGameInstance, gameStore, thread)

        _ <- updateScoreStoreWithLoss(newMaybeGameInstance, gameStore)

        _ <- updateScoreStoreWithWin(newMaybeGameInstance, gameStore)

        scores <- getScores(newMaybeGameInstance, gameStore)

        _ <- scores.traverse_ { score =>
          IO(rtmClient.sendMessage(message.channel, s"$score", Some(thread)))
        }

      //access dynamodb
    }
  }
  // TODO: Dear god loss input please
  def handleGame(gameInstance: GameInstance, command: GameContextCommand, authorId: String, rtmClient: SlackRtmClient, message: Message, gameStore: RDSGameStore, thread: String): IO[Unit] = {

    val (newGameInstance, reply) = CommandInterpreter.interpretGameContextCommand(command, gameInstance, authorId)

    // Then put that stuff

    putarydoo(rtmClient, message, reply, thread, gameStore, newGameInstance)

    // Then if it's a finished, we do the score stuff


  }

  // TODO: Dear god loss input please
  def handleChallenge(challengerId: String, defenderId: String, flags: String, rtmClient: SlackRtmClient, message: Message, gameStore: RDSGameStore, thread: String): IO[Unit] = {

    val (newGameInstance, reply) = CommandHandler.challenge(challengerId, defenderId, flags)

    val io = putarydoo(rtmClient, message, reply, thread, gameStore, newGameInstance)

    newGameInstance match {
        // TODO: Does isInstanceOf work?
      case Finished(rankType) => {
        rankType match {
          case UnRanked => io
          case Ranked(winnerId, loserId) => updateScores(winnerId, loserId, gameStore, io, message, rtmClient, thread)
        }
      }
      case _ => io
    }

  }

  // TODO: Dear god loss input please
  // TODO: Better name
  def putarydoo(rtmClient: SlackRtmClient, message: Message, reply: String, thread: String, gameStore: RDSGameStore, gameInstance: GameInstance): IO[Unit] = {
    for {
      _ <- IO(rtmClient.sendMessage(message.channel, s"<@${message.user}>: $reply", Some(thread)))
      _ <- gameStore.put(thread, gameInstance)
    } yield ()
  }

  def updateScores(winnerId: String, loserId: String, gameStore: RDSGameStore, io: IO[Unit], message: Message, rtmClient: SlackRtmClient, thread: String): IO[Unit] = {
    for {
      _ <- gameStore.updateLoss(loserId)
      _ <- gameStore.updateWin(winnerId)
      scores <- gameStore.reportScores(winnerId, loserId)
      _ <- IO(rtmClient.sendMessage(message.channel, s"<@${message.user}>: $scores", Some(thread)))
    } yield ()
  }

//  def handleNoContextCommand





}
