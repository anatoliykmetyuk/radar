package radar
package telegram

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
class ChatBot(val token: String)
  extends Actor with ActorLogging
  with TelegramBot with Polling with InlineQueries with Commands with Callbacks
  with Helpers with Gui with Logic with InterfaceWiring {

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
}
