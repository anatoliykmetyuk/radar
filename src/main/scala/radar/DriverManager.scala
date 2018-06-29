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

case class Execute[A](code: RemoteWebDriver => A)
case class Result [A](result: A)

object DriverManager {
  def props(workers: Int) = Props(classOf[DriverManager], workers)
}

class DriverManager(workersNum: Int) extends Actor with ActorLogging {
  var workers: List[ActorRef] = List()
  var currentWorker: Int = 0

  def incrementCurrentWorker(): Unit = {
    currentWorker = (currentWorker + 1) % workersNum
  }

  override def preStart(): Unit = {
    log.info(const.log.driverManagerStarted(self.toString))
    workers = (1 to workersNum).map(_ => context actorOf DriverWorker.props).toList
  }

  override def receive = {
    case e @ Execute(_) =>
      workers(currentWorker) forward e
      incrementCurrentWorker()
  }
}

object DriverWorker {
  def props = Props[DriverWorker]
}

class DriverWorker extends Actor with ActorLogging {
  lazy val driver: RemoteWebDriver =
    run { for {
      gridHost <- opt { Option(System.getenv("GRID_HOST")) }
      gridPort <- opt { Option(System.getenv("GRID_PORT")).map(_.toInt) }
      gridUrl   = new URL("http", gridHost, gridPort, "/wd/hub")
      res      <- att { new RemoteWebDriver(gridUrl, new ChromeOptions().setHeadless(true)) }
    } yield res }

  override def preStart(): Unit = {
    log.info(const.log.driverWorkerStarted(self.toString))
  }

  override def postStop(): Unit = {
    driver.quit()
  }

  override def receive = {
    case Execute(code) =>
      val res = code(driver)
      sender ! Result(res)
  }
}


