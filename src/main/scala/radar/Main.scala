package radar

import java.io.File
import org.apache.commons.io.FileUtils

import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.{ ChromeDriver, ChromeDriverService, ChromeOptions }
import org.openqa.selenium.remote.{ RemoteWebDriver, DesiredCapabilities }

import cats._, cats.implicits._


object Main {
  def main(args: Array[String]): Unit = {
    val driverFile = Option(System.getenv("CHROME_DRIVER")).getOrElse("assets/chromedriver")
    println(s"Looking for Chrome at $driverFile")

    val service = new ChromeDriverService.Builder()
      .usingDriverExecutable(new File(driverFile))
      .usingAnyFreePort()
      .build()
    service.start()

    val driver = new RemoteWebDriver(
      service.getUrl()
    , DesiredCapabilities.chrome() merge
      new ChromeOptions().setHeadless(true))

    driver.get("https://www.facebook.com/pg/HUB.4.0/events/")

    val events = driver.findElementById("upcoming_events_card")
    println(events.getAttribute("outerHTML"))

    driver.quit()
    service.stop()
    // FileUtils.writeStringToFile(new File("res.html"), doc.toHtml)
  }
}
