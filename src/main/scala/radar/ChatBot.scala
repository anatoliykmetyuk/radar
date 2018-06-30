package radar

import org.apache.commons.io.FileUtils

import scala.concurrent.duration._, Duration.Zero

import akka.actor._
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query

import info.mukel.telegrambot4s.api._
import info.mukel.telegrambot4s.api.declarative.{Commands, InlineQueries, Callbacks}
import info.mukel.telegrambot4s.models._
import info.mukel.telegrambot4s.methods._

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

  var recipient: Option[Int] = None

  override def preStart(): Unit = {
    log.info("ChatBot started")
    // context.system.scheduler.schedule(Zero, 1 minute, self, Update)  // TODO configure update times externally
    this.run()
  }

  def sendEvt(e: FacebookEvent): Ef[Unit] =
    for {
      to  <- opt { recipient }
      eId <- opt { e.id }
      _   <- att { request(SendMessage(to, e.toString)) }
      _   <- ioe { db.fbevents.markNotified(eId) }
    } yield ()


  // onCommand('start) { implicit msg =>
  //   runR { for {
  //     user <- opt { msg.from }
  //     id    = user.id
  //     _    <- att { self ! SetRecipient(id) }
  //     _    <- att { reply(
  //       s"""Registered your id! Your id is $id.
  //          |Your user entity is $user.
  //          |Will now feed you with the info you requested.
  //       """.stripMargin)}
  //     _    <- att { log.info(const.log.registeredRecipient(recipient.toString)) }
  //   } yield () }
  // }

  // def lmgtfyBtn(query: String): InlineKeyboardMarkup = InlineKeyboardMarkup.singleButton(
  //   InlineKeyboardButton.url("\uD83C\uDDECoogle it now!", lmgtfyUrl(query)))

  val tag = prefixTag("HI") _

  def btn(): InlineKeyboardMarkup = InlineKeyboardMarkup.singleButton(
    InlineKeyboardButton.callbackData(
      s"Press Me", tag("payload")))

  onCallbackWithTag("HI") { implicit cbq =>
    println(s"Callback query received: $cbq")
    request(SendMessage(cbq.from.id, "Responce to CBQ", replyMarkup = Some(btn())))
  }

  onCommand('start) { implicit msg =>
    request(SendMessage(
      msg.from.get.id
    , "Hello"
    , replyMarkup = Some(btn())))
  }

  override def receive = {
    case SetRecipient(id) =>
      recipient = Some(id)
    
    case Update if recipient.isDefined =>
      log.info(const.log.notifying(recipient.toString))
      runR { for {
        evts <- ioe { db.fbevents.listNew }
        _    <- evts.traverse(sendEvt)
      } yield () }
  }
}
