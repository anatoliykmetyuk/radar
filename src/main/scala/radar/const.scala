package radar

object const {
  object log {
    def scrapingTarget(target: String): String =
      s"Scraping target: $target"

    def dbWrite(db: String, strRepr: String): String =
      s"The following content written to the $db database:\n$strRepr"

    def notifying(recipient: String) =
      s"Notifying $recipient now"

    def registeredRecipient(recipient: String) =
      s"Registered recipient: $recipient"

    def fbEventsStarted(page: String) =
      s"FacebookEvents started for target $page"

    def driverManagerStarted(name: String) =
      s"DriverManager started: $name"
    
    def driverWorkerStarted(name: String) =
      s"DriverWorker started: $name"

    def receivedEvents(evts: String, from: String) =
      s"Received $evts events from $from"
  }

  object err {
    val emptyOption = "Empty option"
  }
}