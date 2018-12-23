import akka.actor.ActorSystem
import slack.SlackUtil
import slack.api.SlackApiClient
import slack.rtm.SlackRtmClient

class slackClient {

  // Just example code dump atm
  // https://github.com/slack-scala-client/slack-scala-client

  // test-ers5757

  //...yep
  val token = "TpQUonL9EzkjbfyUD50cAlEf"

  implicit val system = ActorSystem("slack")

  implicit val ec = system.dispatcher

  val client = SlackRtmClient(token)
  val selfId = client.state.self.id

  client.onMessage { message =>
    val mentionedIds = SlackUtil.extractMentionedIds(message.text)

    if(mentionedIds.contains(selfId)) {
      client.sendMessage(message.channel, s"<@${message.user}>: Hey!")
    }
  }
}
