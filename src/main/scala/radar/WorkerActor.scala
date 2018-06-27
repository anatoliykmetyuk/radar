package radar

import java.io.File
import java.net.URL
import org.apache.commons.io.FileUtils

import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.{ ChromeDriver, ChromeDriverService, ChromeOptions }
import org.openqa.selenium.remote.{ RemoteWebDriver, DesiredCapabilities }

import cats._, cats.implicits._
import io.circe.yaml.parser

trait WorkerActor extends EfActor with ActorLogging with EfOnion {
  val driver: RemoteWebDriver =
    runEf { for {
      gridHost <- opt { Option(System.getenv("GRID_HOST")) }
      gridPort <- opt { Option(System.getenv("GRID_PORT")).map(_.toInt) }
      gridUrl   = new URL("http", gridHost, gridPort, "/wd/hub")
    } yield new RemoteWebDriver(gridUrl, new ChromeOptions()) }

  override def preStop(): Unit = driver.quit()

  def url(target: String) =
    s"https://www.facebook.com/pg/$target/events/"

  def scrape(target: String): Unit

  override def receive = {
    case Scrape(target) =>
      val targetUrl = url(target)
      log.info(const.log.scrapingTarget(target, targetUrl))
      
      driver.get(targetUrl)
      val events = driver.findElementById("upcoming_events_card")
      println(events.getAttribute("outerHTML"))
  }
}
