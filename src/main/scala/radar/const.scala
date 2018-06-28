package radar

object const {
  object log {
    def scrapingTarget(target: String): String =
      s"Scraping target: $target"

    def dbWrite(db: String, strRepr: String): String =
      s"The following content written to the $db database:\n$strRepr"
  }

  object err {
    val emptyOption = "Empty option"
  }
}