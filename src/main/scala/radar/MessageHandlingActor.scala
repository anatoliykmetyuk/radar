package radar

import java.io.File
import java.net.URL
import org.apache.commons.io.FileUtils

import scala.collection.JavaConverters._

import org.openqa.selenium.{ WebDriver, WebElement, By }
import org.openqa.selenium.chrome.{ ChromeDriver, ChromeDriverService, ChromeOptions }
import org.openqa.selenium.remote.{ RemoteWebDriver, DesiredCapabilities }

import scala.concurrent.duration._, Duration.Zero
import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor._
import cats._, cats.implicits._

import radar.model.Message

trait MessageHandlingActor { this: ActorLogging =>
  def writeMessagesToDb(msgs: List[Message], format: String, target: Option[String], takeLatest: Int = 100): Ef[Unit] =
    for {
      _ <- att { log.info(const.log.receivedMessages(msgs.length.toString)) }
  
      latest  <- ioe { db.message.listLatest(format, target, Some(takeLatest)) }.map(_.toSet)
      newMsgs  = msgs.filter(e => !latest(e))
      _       <- ioe { newMsgs.traverse(db.message.create) } // TODO batch create
      _        = log.info(const.log.dbWrite(format, newMsgs.mkString("\n")))
    } yield ()
}
