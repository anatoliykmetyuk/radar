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


trait Helpers { this: ChatBot =>
  val state = collection.mutable.Map[Int, states.DialogueState]()
  
  object states {
    sealed trait DialogueState
    case object Protocol                 extends DialogueState
    case class  Target(protocol: String) extends DialogueState
    case object Empty                    extends DialogueState
    case object ChannelName              extends DialogueState
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