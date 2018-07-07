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

import radar.model._

case object RequestingKey
case class GotKey(k: String)

object Codementor {
  def props(driverManager: ActorRef, chatBot: ActorRef) =
    Props(classOf[Codementor], driverManager, chatBot)
}

class Codementor(driverManager: ActorRef, chatBot: ActorRef) extends Actor
    with ActorLogging with MessageHandlingActor {
  val format = "codementor"
  var key: Option[String] = None

  override def preStart(): Unit = {
    log.info(const.log.codementorStarted)
    context.system.scheduler.schedule(1 minute, 15 minutes, self, Update)  // TODO configure update times externally
  }

  override def receive = {
    case Update if key.isEmpty =>
      log.info("Cannot find the key, requesting it")
      chatBot ! RequestingKey

    case GotKey(k) =>
      log.info("Key is successfully registered")
      key = Some(k)
      self ! Update

    case Update if key.isDefined =>
      log.info("Starting Codementor task...")
      run { for {
        k              <- opt { key }
        credentialsOpt <- ioe { db.credentials.get("google") }
        credentialsEnc <- opt (credentialsOpt, "No credentials found for Google")
        credentials    <- att { credentialsEnc.decrypted(k) }
        _              <- att { driverManager ! Execute(scrape(credentials)) }
      } yield () }

    case Result(msgs: List[Message]) =>
      run { writeMessagesToDb(msgs, format, None) }
  }

  def scrape(credentials: Credentials): RemoteWebDriver => List[Message] = { driver =>
    val targetUrl = "https://www.codementor.io/m/dashboard/open-requests?expertise=related"
    
    log.info("Trying to access the target page")
    driver.get(targetUrl)
    if (driver.getCurrentUrl.contains("login")) {
      log.info("Log-in needed")
      login(credentials, driver)
      driver.get(targetUrl)
    }

    log.info(s"Successfully obtained the target page, starting scraping. Page: ${driver.getCurrentUrl}")
    // Parsing proper
    driver.findElements(By.className("dashboard__open-question-item"))
      .asScala.map(parseRequest).toList
  }

  def login(credentials: Credentials, driver: RemoteWebDriver): Unit = {
    def l(msg: String) = log.info(s"CODEMENTOR: $msg")

    l("Login sequence started")
    // Login sequence
    val loginBtn = driver.findElement(By.xpath("""/html/body/div[2]/div/div[3]/div/div/form/div[5]/a[3]"""))
    loginBtn.click()

    val email = driver.findElement(By.xpath("""//*[@id="identifierId"]"""))
    email.sendKeys(credentials.login)
    driver.findElement(By.xpath("""//*[@id="identifierNext"]""")).click()

    l("Email submitted")
    Thread.sleep(1500)

    val password = driver.findElement(By.xpath("""//*[@id="password"]/div[1]/div/div[1]/input"""))
    password.sendKeys(credentials.password)
    driver.findElement(By.xpath("""//*[@id="passwordNext"]""")).click()

    Thread.sleep(3000)
    l(s"Successfully completed the log in sequence. Current page: ${driver.getCurrentUrl}")
  }

  def parseRequest(e: WebElement): Message = {
    def c(cls: String): String = e.findElement(By.className(cls)).getText
    val link       = e.getAttribute("href")
    val title      = c("content-row__header__title")
    val tags       = c("content-row__header__tags" )
    val interested = c("content-row__interest"     )
    val created    = c("content-row__created-at"   )
    val money      = c("content-row__budget"       )

    Message(
      format = format
    , link   = link
    , text   = s"$title\n$tags\n${"$"}$money\n$interested\n$created")
  }
}
