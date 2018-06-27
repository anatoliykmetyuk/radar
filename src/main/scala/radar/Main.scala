package radar

import java.io.File
import java.net.URL
import org.apache.commons.io.FileUtils

import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.{ ChromeDriver, ChromeDriverService, ChromeOptions }
import org.openqa.selenium.remote.{ RemoteWebDriver, DesiredCapabilities }

import cats._, cats.implicits._


object Main {
  def localServiceInit(): (URL, () => Unit) = {
    val driverFile = new File("assets/chromedriver")
    println(s"Looking for Chrome Driver at $driverFile")

    val service = new ChromeDriverService.Builder()
      .usingDriverExecutable(driverFile)
      .usingAnyFreePort()
      .build()
    service.start()

    (service.getUrl(), () => service.stop())
  }

  def getServiceUrl(): (URL, () => Unit) =
    (for {
      gridHost <- Option(System.getenv("GRID_HOST"))
      gridPort <- Option(System.getenv("GRID_PORT")).map(_.toInt)
    } yield (new URL("http", gridHost, gridPort, "/wd/hub"), () => ()) )
      .getOrElse(localServiceInit())

  def main(args: Array[String]): Unit = {
    val (gridUrl, cleanup) = getServiceUrl()
    println(s"Grid URL is: $gridUrl")

    val driver = new RemoteWebDriver(
      gridUrl
    , new ChromeOptions())

    driver.get("https://www.facebook.com/pg/HUB.4.0/events/")

    val events = driver.findElementById("upcoming_events_card")
    println(events.getAttribute("outerHTML"))

    driver.quit()
    cleanup()
    // FileUtils.writeStringToFile(new File("res.html"), doc.toHtml)
  }
}
