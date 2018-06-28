package radar

object const {
  object log {
    def scrapingTarget(target: String): String =
      s"Scraping target: $target"
  }

  object err {
    val emptyOption = "Empty option"
  }
}