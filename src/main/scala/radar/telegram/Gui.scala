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


trait Gui { this: ChatBot =>
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