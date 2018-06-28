package radar

import java.io.File
import java.net.URL
import org.apache.commons.io.FileUtils

import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.{ ChromeDriver, ChromeDriverService, ChromeOptions }
import org.openqa.selenium.remote.{ RemoteWebDriver, DesiredCapabilities }

import akka.actor._
import cats._, cats.implicits._
import io.circe.yaml.parser


object Main {

  def main(args: Array[String]): Unit = {
    val as = ActorSystem("RadarActors")
    as actorOf Props[FacebookEvents]
  }
}
