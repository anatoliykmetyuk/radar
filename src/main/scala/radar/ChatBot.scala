package radar

import org.apache.commons.io.FileUtils

import scala.concurrent.duration._, Duration.Zero

import akka.actor._
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.model.Uri.Query

import info.mukel.telegrambot4s.api._
import info.mukel.telegrambot4s.api.declarative.{ Commands, InlineQueries, Action }
import info.mukel.telegrambot4s.models._
import info.mukel.telegrambot4s.methods._

import io.circe.yaml
import cats._, cats.implicits._, cats.effect._, cats.data.{ NonEmptyList => NEL, _ }

import radar.{ run => runR }
import radar.model.{ Message => RMessage, _ }


case class SetRecipient(to: Int, key: String)

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
                                    with Commands {

  var recipient: Option[Int   ] = None
  var key      : Option[String] = None

  override def preStart(): Unit = {
    log.info("ChatBot started")
    context.system.scheduler.schedule(Zero, 1 minute, self, Update)  // TODO configure update times externally
    this.run()
  }

  def sendMsg(e: RMessage): Ef[Unit] =
    for {
      to  <- opt { recipient }
      eId <- opt { e.id }
      _   <- att { request(SendMessage(to, e.toString)) }
      _   <- ioe { db.message.markNotified(eId) }
    } yield ()

  def withArgsLst(action: Action[List[String]])(implicit msg: Message): Unit =
    withArgs { args => action(args.toList) }


  onCommand('start) { implicit msg =>
    withArgsLst {
      case Nil    => reply("Please specify your codephrase")
      case key :: _ =>
        runR { for {
          user <- opt { msg.from }
          id    = user.id
          _    <- att { self ! SetRecipient(id, key) }
          _    <- att { reply(
            s"""Registered your id! Your id is $id.
               |Your user entity is $user. Your key is $key.
               |Will now feed you with the info you requested.
            """.stripMargin)}
          _    <- att { log.info(const.log.registeredRecipient(recipient.toString)) }
        } yield () }
    }
  }

  onCommand('credentials) { implicit msg =>
    withArgsLst { case target :: login :: password :: _ =>
      runR { for {
        k           <- opt { key }
        credentials <- att { Credentials(
          target   = target
        , login    = login
        , password = password).encrypted(k) }
        _ <- ioe { db.credentials.create(credentials) }
        _ <- att { reply(s"Your credentials are saved as $credentials.") }
      } yield () }
    }
  }

  override def receive = {
    case SetRecipient(id, k) =>
      recipient = Some(id)
      key = Some(k)
    
    case Update if recipient.isDefined =>
      log.info(const.log.notifying(recipient.toString))
      runR { for {
        msgs <- ioe { db.message.listNew() }
        _    <- msgs.traverse(sendMsg)
      } yield () }

    case RequestingKey => key match {
      case Some(k) => sender ! GotKey(k)
      case None =>
    }
  }
}
