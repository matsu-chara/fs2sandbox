
import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import fs2.concurrent.SignallingRef
import scala.concurrent.duration._

object Concurrent extends IOApp {
  sealed trait Message
  case class HealthCheckMessage(result: Boolean) extends Message
  case class ErrorMessage(cause: Throwable) extends Message

  def healthCheck: Stream[IO, Message] = ???

  def kafkaMessages: Stream[IO, Message] = ???

  def celsiusConverter: Stream[IO, Unit] = ???

  def all: Stream[IO, Message] =
    Stream(
      healthCheck,
      celsiusConverter.drain,
      kafkaMessages
    ).covary[IO].parJoinUnbounded

  def stopAfter[A](in: Stream[IO, A], f: FiniteDuration): Stream[IO, A] = {
    def out(s: SignallingRef[IO, Boolean]): Stream[IO, A] =
      in.interruptWhen(s)

    def stop(s: SignallingRef[IO, Boolean]): Stream[IO, Unit] =
      Stream.sleep_[IO](f) ++ Stream.eval(s.set(true))

    Stream.eval(SignallingRef[IO, Boolean](false)).flatMap { stopSignal =>
      val stopper = stop(stopSignal)
      val runner = out(stopSignal)
      runner.concurrently(stopper)
    }
  }

  def stopAfter[A](f: FiniteDuration): Stream[IO, A] => Stream[IO, A] = {
    in => {
      def close(s: SignallingRef[IO, Boolean]): Stream[IO, Unit] =
        Stream.sleep_[IO](f) ++ Stream.eval(s.set(true))

      Stream.eval(SignallingRef[IO, Boolean](false)).flatMap { end =>
        in.interruptWhen(end).concurrently(close(end))
      }
    }
  }

  override def run(args: List[String]): IO[ExitCode] = {
    Stream
      .repeatEval(IO(println("hello")))
      .through(stopAfter(2.seconds))
      .compile.drain.map(_ => ExitCode.Success)
  }
}
