package radar

import java.io.File
import java.net.URL
import org.apache.commons.io.FileUtils

import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.{ ChromeDriver, ChromeDriverService, ChromeOptions }
import org.openqa.selenium.remote.{ RemoteWebDriver, DesiredCapabilities }

import scala.concurrent.duration._, Duration.Zero
import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor._
import cats._, cats.implicits._
import io.circe.yaml.parser

case object Update

class FacebookEvents extends Actor with ActorLogging {
  val target = "https://www.facebook.com/pg/HUB.4.0/events/"

  val driver: RemoteWebDriver =
    run { for {
      gridHost <- opt { Option(System.getenv("GRID_HOST")) }
      gridPort <- opt { Option(System.getenv("GRID_PORT")).map(_.toInt) }
      gridUrl   = new URL("http", gridHost, gridPort, "/wd/hub")
    } yield new RemoteWebDriver(gridUrl, new ChromeOptions()) }

  override def preStart(): Unit = {
    context.system.scheduler.schedule(Zero, 15 seconds, self, Update)
  }

  override def postStop(): Unit = {
    driver.quit()
  }

  override def receive = {
    case Update =>
      log.info(const.log.scrapingTarget(target))
      
      driver.get(target)
      val events = driver.findElementById("upcoming_events_card")
      println(events.getAttribute("outerHTML"))
  }
}
