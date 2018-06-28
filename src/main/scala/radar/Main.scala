package radar

import java.io.File
import java.net.URL
import org.apache.commons.io.FileUtils

import scala.concurrent.Await
import scala.concurrent.duration._

import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.{ ChromeDriver, ChromeDriverService, ChromeOptions }
import org.openqa.selenium.remote.{ RemoteWebDriver, DesiredCapabilities }

import akka.actor._
import cats._, cats.implicits._
import io.circe.yaml.parser


object Main {

  val targets = List(
    "HUB.4.0"
  , "CashflowinKieve")

  def main(args: Array[String]): Unit =
    run { for {
      // Read the telegram token from the configuration
      config  <- att { FileUtils.readFileToString(new java.io.File("radar.yml"), settings.enc) }
      cfgJson <- exn { parser.parse(config) }
      token   <- exn { cfgJson.hcursor.get[String]("telegram-token") }

      // Bootstrap actors
      as <- att { ActorSystem("RadarActors")       }
      _  <- targets.traverse { t => att { as actorOf FacebookEvents.props(t) } }
      _  <- att { as actorOf ChatBot.props(token)  }

      // Shutdown hook
      _  <- att { scala.sys.addShutdownHook {
              println("Termination started")
              as.terminate()
              Await.result(as.whenTerminated, Duration.Inf)
              println("Terminated")
            } }
    } yield () }
}
