import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import scala.concurrent.duration._

object HealthCheck extends IOApp {
  sealed trait Message
  case class HealthCheckMessage(result: Boolean) extends Message
  case class ErrorMessage(cause: Throwable) extends Message

  def healthCheck: IO[Boolean] = IO {
      true
  }

  def healthCheck2: Stream[IO, Message] = {
    val retryCheck = Stream.retry(
      fo = healthCheck,
      delay = 1.second,
      nextDelay = _ + 1.second,
      maxAttempts = 5
    )

    val check =
      retryCheck.map(HealthCheckMessage)
      .handleErrorWith(cause => Stream.emit(ErrorMessage(cause)))

    val repeatedChecks = (check ++ Stream.sleep_(1.hour)).repeat

    repeatedChecks
  }

  override def run(args: List[String]): IO[ExitCode] = {
    healthCheck2.compile.drain.map(_ => ExitCode.Success)
  }

}
