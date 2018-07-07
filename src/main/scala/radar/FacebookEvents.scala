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

case object Update

object FacebookEvents {
  def props(target: String, driverManager: ActorRef) =
    Props(classOf[FacebookEvents], target, driverManager)
}

class FacebookEvents(target: String, driverManager: ActorRef) extends Actor
    with ActorLogging with MessageHandlingActor {
  val page = s"https://www.facebook.com/pg/$target/events/"

  val format = "fbevent"

  override def preStart(): Unit = {
    log.info(const.log.fbEventsStarted(page))
    context.system.scheduler.schedule(Zero, 3 hours, self, Update)  // TODO configure update times externally
  }

  override def receive = {
    case Update =>
      log.info(const.log.scrapingTarget(page))
      val code: RemoteWebDriver => List[Message] = { driver =>
        driver.get(page)
        driver
          .findElements(By.xpath("""//*[@id="upcoming_events_card"]/div/div[@class="_24er"]""")).asScala
          .map(parseEvent).toList
      }
      driverManager ! Execute(code)

    case Result(events: List[Message]) =>
      run { writeMessagesToDb(events, format, Some(target)) }
  }

  def parseEvent(e: WebElement): Message = {
    def xp(path: String) = e.findElement(By.xpath(path))

    val month = xp("table/tbody/tr/td[1]/span/span[1]").getText
    val date  = xp("table/tbody/tr/td[1]/span/span[2]").getText

    val nameLinkElem = xp("table/tbody/tr/td[2]/div/div[1]/a")
    val name = nameLinkElem.getText
    val link = nameLinkElem.getAttribute("href")
    val details = xp("table/tbody/tr/td[2]/div/div[2]").getText

    Message(
      format = "fbevent"
    , target = Some(target)
    , link   = link
    , text   = s"$name\n$details\n$date $month")
  }
}
