package radar

import cats._, cats.implicits._
import io.circe.yaml.parser


class FormatScraper(
  cfg        : ScraperConfig
, workerProps: Props)
extends Actor with ActorLogging with EfOnion {
  override def preStart(): Unit = {
    context.system.scheduler(Zero, cfg.period seconds, self, Update)
  }

  def spawnWorkers(): List[ActorRef] =
    (1 to cfg.workersNum).map { _ => context.actorOf(workerProps) }

  override def receive = {
    case Update =>
      val workers: List[ActorRef] = spawnWorkers()
      val batches: List[List[String]] =
        targets.sliding(workers.length, workers.length)

      for {
        (w, ts) <- workers zip batches
        t <- ts
      } w ! Scrape(t)
  }
}
