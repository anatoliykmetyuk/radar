package radar

import java.io.File
import java.net.URL
import org.apache.commons.io.FileUtils

import scala.collection.JavaConverters._

import org.openqa.selenium.{ WebDriver, WebElement, By, Cookie }
import org.openqa.selenium.chrome.{ ChromeDriver, ChromeDriverService, ChromeOptions }
import org.openqa.selenium.remote.{ RemoteWebDriver, DesiredCapabilities }

import cats._, cats.implicits._


object MainLocal {
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

    driver.get("https://www.codementor.io/login")
    
    // Login sequence
    val loginBtn = driver.findElement(By.xpath("""/html/body/div[2]/div/div[3]/div/div/form/div[5]/a[3]"""))
    loginBtn.click()

    val email = driver.findElement(By.xpath("""//*[@id="identifierId"]"""))
    email.sendKeys("MASKED")
    driver.findElement(By.xpath("""//*[@id="identifierNext"]""")).click()

    Thread.sleep(1500)

    val password = driver.findElement(By.xpath("""//*[@id="password"]/div[1]/div/div[1]/input"""))
    password.sendKeys("MASKED")
    driver.findElement(By.xpath("""//*[@id="passwordNext"]""")).click()

    Thread.sleep(3000)

    driver.get("https://www.codementor.io/m/dashboard/open-requests?expertise=related")

    case class CodementorRequest(
      title     : String
    , link      : String
    , tags      : String
    , interested: String
    , created   : String
    , money     : String)

    object CodementorRequest extends Function1[WebElement, CodementorRequest] {
      def apply(e: WebElement): CodementorRequest =
        CodementorRequest(
          link       = e.getAttribute("href")
        , title      = e.findElement(By.className("content-row__header__title")).getText
        , tags       = e.findElement(By.className("content-row__header__tags")) .getText
        , interested = e.findElement(By.className("content-row__interest"))     .getText
        , created    = e.findElement(By.className("content-row__created-at"))   .getText
        , money      = e.findElement(By.className("content-row__budget"))       .getText)
    }

    val reqs: List[CodementorRequest] = driver.findElements(By.className("dashboard__open-question-item"))
      .asScala.map(CodementorRequest).toList

    println(reqs.mkString("\n"))


    // val events: List[FacebookEvent] = driver
    //   .findElements(By.xpath("""//*[@id="upcoming_events_card"]/div/div[@class="_24er"]""")).asScala
    //   .map(FacebookEvent(_, "HUB.4.0")).toList

    // println(events.mkString("\n"))

    // driver.quit()
    // cleanup()
    // FileUtils.writeStringToFile(new File("res.html"), doc.toHtml)
  }
}
