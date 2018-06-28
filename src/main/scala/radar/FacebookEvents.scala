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
  def props(page: String) = Props(classOf[FacebookEvents], page)
}

class FacebookEvents(page: String) extends Actor with ActorLogging {
  val target = s"https://www.facebook.com/pg/$page/events/"

  lazy val driver: RemoteWebDriver =
    run { for {
      gridHost <- opt { Option(System.getenv("GRID_HOST")) }
      gridPort <- opt { Option(System.getenv("GRID_PORT")).map(_.toInt) }
      gridUrl   = new URL("http", gridHost, gridPort, "/wd/hub")
      res      <- att { new RemoteWebDriver(gridUrl, new ChromeOptions()) }
    } yield res }

  override def preStart(): Unit = {
    log.info(s"FacebookEvents started for target $page")
    context.system.scheduler.schedule(Zero, 15 seconds, self, Update)
  }

  override def postStop(): Unit = {
    driver.quit()
  }

  override def receive = {
    case Update =>
      log.info(const.log.scrapingTarget(target))
      driver.get(target)
      val events: List[FacebookEvent] = driver
        .findElements(By.xpath("""//*[@id="upcoming_events_card"]/div/div[@class="_24er"]""")).asScala
        .map(FacebookEvent(_, page)).toList

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
