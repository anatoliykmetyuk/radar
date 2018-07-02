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


case class SetRecipient(to: Int)

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
                                    with Callbacks {

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

  onCommand('start) { implicit msg =>
    self ! Start(msg)
  }

  def mainMenu() = InlineKeyboardMarkup.singleColumn(Seq(
    InlineKeyboardButton.callbackData("Subscriptions", (Subscriptions: RadarCommand).asJson.pretty(Printer.noSpaces))
  , InlineKeyboardButton.callbackData("Channels"     , (Channels: RadarCommand)     .asJson.pretty(Printer.noSpaces))
  ))

  def subscriptions() = InlineKeyboardMarkup.singleColumn(Seq(
    InlineKeyboardButton.callbackData("Main Menu", (MainMenu: RadarCommand).asJson.pretty(Printer.noSpaces))
  ))

  def channels() = InlineKeyboardMarkup.singleColumn(Seq(
    InlineKeyboardButton.callbackData("Main Menu", (MainMenu: RadarCommand).asJson.pretty(Printer.noSpaces))
  ))

  override def receive = {
    case Start(msg) =>
      // TODO check if user exists in the database. If not, add them.
      runR { for {
        user <- opt { msg.from }
        _    <- att { request(SendMessage(user.id, "Main Menu", replyMarkup = Some(mainMenu()))) }
      } yield () }

    case RadarCommandWrapper(x, cbq) => x match {
      case MainMenu =>
        request(SendMessage(cbq.from.id, "Main Menu", replyMarkup = Some(mainMenu())))

      case Subscriptions =>
        request(SendMessage(cbq.from.id, "Your Subscriptions", replyMarkup = Some(subscriptions())))
      
      case Channels =>
        request(SendMessage(cbq.from.id, "Your Channels", replyMarkup = Some(channels())))
    }
    

  }
}

case class Start(msg: TMessage)
case class RadarCommandWrapper(cmd: RadarCommand, cbq: CallbackQuery)

sealed trait RadarCommand
case object Subscriptions extends RadarCommand
case object Channels      extends RadarCommand
case object MainMenu      extends RadarCommand
