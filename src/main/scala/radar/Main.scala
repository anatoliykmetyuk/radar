package radar

import java.io.File
import java.net.URL
import org.apache.commons.io.FileUtils

import org.openqa.selenium.WebDriver
import org.openqa.selenium.chrome.{ ChromeDriver, ChromeDriverService, ChromeOptions }
import org.openqa.selenium.remote.{ RemoteWebDriver, DesiredCapabilities }

import cats._, cats.implicits._
import io.circe.yaml.parser


object Main {

  def main(args: Array[String]): Unit = {
    val availableScrapers: Map[String, Props] = Map(
      const.format.facebookEvent -> FacebookEvents.props )

    val as = ActorSystem("RadarActors")

    run { for {
      // Read the radar.yml config file
      radarSpecRaw <- att { FileUtils.readFileToString(new File("radar.yml"), settings.enc) }
      
      // Build the configuration out of that file
      json         <- exn { parser.parse(radarSpecRaw) }
      targetsJson  <- exn { json.hcursor.downField(const.radarSpec.targetsKey) }
      formats      <- opt { targetsJson.hcursor.keys }
      targets      <- formats.traverse { k =>
        json.hcursor.downField(k).as[ScraperConfig].map { cfg =>
          defaultCfg |+| cfg.copy(name = Some(k)) } }

      // For each config entry, start a service actor
      _ <- targets.traverse { cfg =>
        for {
          name  <- opt { cfg.name }
          props <- availableScrapers.get(cfg.name)
          actor <- app { as actorOf props }
        } yield () }
    } yield () }
  }
}
