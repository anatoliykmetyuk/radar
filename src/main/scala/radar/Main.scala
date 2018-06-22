package radar

import net.ruippeixotog.scalascraper.browser.HtmlUnitBrowser
import cats._, cats.implicits._

object Main {
  def main(args: Array[String]): Unit = {

    val browser = HtmlUnitBrowser()
    val doc = browser.parseFile("core/src/test/resources/example.html")
    val doc2 = browser.get("http://example.com")
  }
}
