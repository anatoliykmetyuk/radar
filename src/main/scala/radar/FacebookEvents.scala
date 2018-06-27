package radar

import java.io.File
import java.net.URL
import org.apache.commons.io.FileUtils

import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.{ ChromeDriver, ChromeDriverService, ChromeOptions }
import org.openqa.selenium.remote.{ RemoteWebDriver, DesiredCapabilities }

import cats._, cats.implicits._
import io.circe.yaml.parser

object FacebookEvents {
  val props = Props[FacebookEvents]
}

class FacebookEvents extends WorkerActor with ActorLogging with EfOnion {
  def url(target: String) =
    s"https://www.facebook.com/pg/$target/events/"

  override def receive = {
    case Scrape(target) =>
      val targetUrl = url(target)
      log.info(const.log.scrapingTarget(target, targetUrl))
      
      driver.get(targetUrl)
      val events = driver.findElementById("upcoming_events_card")
      println(events.getAttribute("outerHTML"))
  }
}
