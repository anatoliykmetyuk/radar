package radar

import org.apache.commons.io.FileUtils

import scala.concurrent.duration._, Duration.Zero

import akka.actor._
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query

import info.mukel.telegrambot4s.api._
import info.mukel.telegrambot4s.api.declarative.{Commands, InlineQueries, Callbacks}
import info.mukel.telegrambot4s.models.{ Message => TMessage, _ }
import info.mukel.telegrambot4s.methods._

import io.circe._, io.circe.generic.auto._, io.circe.parser._, io.circe.syntax._
import io.circe.yaml

import cats._, cats.implicits._, cats.effect._, cats.data.{ NonEmptyList => NEL, _ }

import radar.{ run => runR }
import radar.model._


object ChatBot {
  def props(token: String) = Props(classOf[ChatBot], token)
}

/**
  * Let me Google that for you!
  */
class ChatBot(val token: String) extends Actor
                                    with ActorLogging
                                    with TelegramBot
                                    with Polling
                                    with InlineQueries
                                    with Commands
                                    with Callbacks
                                    with ChatBotHelpers 
                                    with ChatBotGui {

  override def preStart(): Unit = {
    log.info("ChatBot started")
    // context.system.scheduler.schedule(Zero, 1 minute, self, Update)  // TODO configure update times externally
    this.run()
  }


  onCallbackQuery { implicit cbq =>
    runR { for {
      data    <- opt { cbq.data } 
      _       <- att { println(data) }
      cmdJson <- exn { parse(data) }
      cmd     <- exn { cmdJson.as[RadarCommand] }
      _       <- att { self ! (RadarCommandWrapper(cmd, cbq)) }
    } yield () }
  }

  onCommand('start) { msg => self ! Start(msg) }
  onMessage { msg => self ! msg }


  override def receive = {
    case Start(msg) =>
      // TODO check if user exists in the database. If not, add them.
      runR { for {
        user <- opt { msg.from }
        _    <- att { request(SendMessage(user.id, "Main Menu", replyMarkup = Some(mainMenu()))) }
      } yield () }

    case msg: TMessage =>
      runR { for {
        from <- opt { msg.from }
        s     = state.getOrElse(from.id, states.empty)
        _    <- att { handleState(s, from.id)(msg) }
      } yield () }

    case RadarCommandWrapper(x, cbq) =>
      implicit val c = cbq
      x match {
        case MainMenu      => msg("Main Menu", mainMenu())
        case Subscriptions =>
          val subs = List(Subscription(Some(1), 1, "fbevent"))
          msg("Your Subscriptions", subscriptions(subs))

        case Channels =>
          val chs = List(Channel(Some(1), "work", 1))
          msg("Your Channels", channels(chs))

        case CancelCmd =>
          state(cbq.from.id) = states.empty
          msg(" ", mainMenu())

        case Subscribe =>
          state(cbq.from.id) = states.protocol
          msg("Please enter the protocol", cancel())

        case NewChannel =>
          state(cbq.from.id) = states.channelName
          msg("Please enter the name of the channel", cancel())

        case ChannelCmd(cid) =>
          msg(
            s"You are viewing channel with id $cid"
          , colI(
              "Main menu" -> MainMenu
            , "Back"      -> Channels
            )
          )

        case SubscriptionCmd(sid) =>
          msg(
            s"You are viewing subscriptions with id $sid"
          , colI(
              "Main menu" -> MainMenu
            , "Back"      -> Subscriptions
            )
          )
      }
  }

  def handleState(s: String, from: Int)(implicit m: TMessage): Unit = {
    import states._
    s match {
      case `protocol` =>
        state(from) = target
        msg(s"Got protocol: ${m.text.get}. Please enter target.")

      case `target` =>
        state(from) = empty
        msg(s"Got target: ${m.text.get}.")

      case `channelName` =>
        state(from) = empty
        msg(s"Got channel name: ${m.text.get}")

      case `empty` => log.info(s"Empty state message: $m")
    }
  }
}

trait ChatBotHelpers { this: ChatBot =>
  val state = collection.mutable.Map[Int, String]()
  object states {
    val protocol    = "protocol"
    val target      = "target"
    val empty       = "empty"
    val channelName = "channel_name"
  }

  def btn(name: String, cmd: RadarCommand): InlineKeyboardButton =
    InlineKeyboardButton.callbackData(name, cmd.asJson.pretty(Printer.noSpaces))

  def col(btns: List[(String, RadarCommand)]) =
    InlineKeyboardMarkup.singleColumn(btns.map((btn _).tupled))

  def colI(btns: (String, RadarCommand)*) = col(btns.toList)

  def row(btns: List[(String, RadarCommand)]) =
    InlineKeyboardMarkup.singleRow(btns.map((btn _).tupled))

  def msg(text: String)(implicit to: User) =
    request(SendMessage(to.id, text))
  
  def msg(text: String, markup: ReplyMarkup)(implicit to: User) =
    request(SendMessage(to.id, text, replyMarkup = Some(markup)))

  implicit def msg2user(implicit m: TMessage     ): User = runR { opt { m.from } }
  implicit def cbq2user(implicit c: CallbackQuery): User = c.from
}

trait ChatBotGui { this: ChatBot =>
  def mainMenu() = col(List(
    "Subscriptions" -> Subscriptions
  , "Channels"      -> Channels
  ))

  def subscriptions(subs: List[Subscription]) = col(
    subs.map { s => s"${s.protocol}:${s.target}" -> SubscriptionCmd(s.id.get) } ++ List(
    "Main Menu" -> MainMenu
  , "New"       -> Subscribe
  ))

  def channels(chs: List[Channel]) = col(
    chs.map { c => c.name -> ChannelCmd(c.id.get) } ++ List(
    "Main Menu" -> MainMenu
  , "New"       -> NewChannel
  ))

  def cancel() = InlineKeyboardMarkup.singleButton(btn("Cancel", CancelCmd))
}

case class Start(msg: TMessage)
case class RadarCommandWrapper(cmd: RadarCommand, cbq: CallbackQuery)

sealed trait RadarCommand
case object Subscriptions extends RadarCommand
case object Channels      extends RadarCommand
case object MainMenu      extends RadarCommand
case object CancelCmd     extends RadarCommand

case object Subscribe     extends RadarCommand
case object NewChannel    extends RadarCommand

case class SubscriptionCmd(id: Int) extends RadarCommand
case class ChannelCmd     (id: Int) extends RadarCommand
