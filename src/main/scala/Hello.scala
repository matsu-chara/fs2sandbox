import java.nio.file.Paths

import cats.effect.{Blocker, IO}
import fs2.{io, text}

object Hello {
  implicit val cs = IO.contextShift(scala.concurrent.ExecutionContext.global)

  def readall(filename: String, blocker: Blocker): fs2.Stream[IO, String] = {
    io.file
      .readAll[IO](Paths.get(s"testdata/$filename"), blocker, 4092)
      .through(text.utf8Decode)
      .through(text.lines)

  }

  def main(args: Array[String]): Unit = {
    println("hello")
    Blocker[IO].use { inBlocker =>
      Blocker[IO].use { outBlocker =>
        readall("file.txt", inBlocker)
          .filter(s => s.nonEmpty && !s.startsWith("//"))
          .intersperse("\n")
          .through(text.utf8Encode)
          .through(io.file.writeAll[IO](Paths.get(s"testdata/out.txt"), outBlocker))
          .compile
          .drain
      }
    }
    .unsafeRunSync()
  }
}
