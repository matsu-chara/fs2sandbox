import cats.effect.{ExitCode, IO, IOApp}
import fs2.Stream

object Sandbox extends IOApp {
  private def put(s: String) = IO(println(s))

  def hhhw(): Stream[IO, Unit] = {
    Stream.repeatEval(put("hello")).take(3) ++ Stream.eval(put("world"))
  }

  def ootttt(): Stream[IO, Unit] = {
    Stream(1,2,3).flatMap(x => Stream.repeatEval(put(x.toString)).take(2))
  }

  override def run(args: List[String]): IO[ExitCode] = {
    hhhw().compile.drain.map(_ => ExitCode.Success)
  }
}
