import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream
import scala.concurrent.duration._

object Sandbox extends IOApp {
  private def put(s: String) = IO(println(s))
  private def putRange: Stream[IO, Unit] = Stream.range(1, 10).evalMap(x => put(x.toString))
  private def seconds: Stream[IO, FiniteDuration] = Stream.awakeEvery[IO](1.second)

  def hhhw(): Stream[IO, Unit] = {
    Stream.repeatEval(put("hello")).take(3) ++ Stream.eval(put("world"))
  }

  def ootttt(): Stream[IO, Unit] = {
    Stream(1,2,3).flatMap(x => Stream.repeatEval(put(x.toString)).take(2))
  }

  def sec(): Stream[IO, (FiniteDuration, Unit)] = {
    seconds.zip(putRange)
  }

  override def run(args: List[String]): IO[ExitCode] = {
    sec().compile.drain.map(_ => ExitCode.Success)
  }
}
