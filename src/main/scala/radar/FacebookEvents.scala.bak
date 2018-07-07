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

case object Update

object FacebookEvents {
  def props(page: String, driverManager: ActorRef) =
    Props(classOf[FacebookEvents], page, driverManager)
}

class FacebookEvents(page: String, driverManager: ActorRef) extends Actor with ActorLogging {
  val target = s"https://www.facebook.com/pg/$page/events/"

  override def preStart(): Unit = {
    log.info(const.log.fbEventsStarted(page))
    context.system.scheduler.schedule(Zero, 1 hour, self, Update)  // TODO configure update times externally
  }

  override def receive = {
    case Update =>
      log.info(const.log.scrapingTarget(target))
      val code: RemoteWebDriver => List[FacebookEvent] = { driver =>
        driver.get(target)
        driver
          .findElements(By.xpath("""//*[@id="upcoming_events_card"]/div/div[@class="_24er"]""")).asScala
          .map(FacebookEvent(_, page)).toList
      }
      driverManager ! Execute(code)

    case Result(events: List[FacebookEvent]) =>
      log.info(const.log.receivedEvents(events.length.toString, sender.toString))
      run { for {
        latest   <- ioe { db.fbevents.listLatest(10) }.map(_.toSet)
        newEvents = events.filter(!latest(_))
        _        <- ioe { newEvents.traverse(db.fbevents.create) } // TODO batch create
        _         = log.info(const.log.dbWrite("fbevents", newEvents.mkString("\n")))
      } yield () }
  }
}

case class FacebookEvent(
    id      : Option[Int] = None
  , month   : String
  , date    : String
  , name    : String
  , link    : String
  , details : String
  , source  : String
  , created : Long    = time.now
  , notified: Boolean = false)
{
  override def toString() =
    s"$source\t$month $date\t$name\t$link\t$details"

  override def equals(that: Any): Boolean = that match {
    case FacebookEvent(_, month, date, name, _, _, source, _, _) =>
      this.month == month && this.date == date && this.name == name && this.source == source
    case _ => false
  }

  override def hashCode(): Int =
    month.hashCode + date.hashCode + name.hashCode + source.hashCode
}

object FacebookEvent {
  def apply(e: WebElement, source: String): FacebookEvent = {
    def xp(path: String) = e.findElement(By.xpath(path))

    val month = xp("table/tbody/tr/td[1]/span/span[1]").getText
    val date  = xp("table/tbody/tr/td[1]/span/span[2]").getText

    val nameLinkElem = xp("table/tbody/tr/td[2]/div/div[1]/a")
    val name = nameLinkElem.getText
    val link = nameLinkElem.getAttribute("href")
    val details = xp("table/tbody/tr/td[2]/div/div[2]").getText

    FacebookEvent(
      month   = month
    , date    = date
    , name    = name
    , link    = link
    , details = details
    , source  = source)
  }
}
