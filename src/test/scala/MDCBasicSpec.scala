import monix.eval.Task
import monix.execution.Scheduler
import org.scalactic.source.Position
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, Matchers}
import org.slf4j.MDC

class MDCBasicSpec extends AsyncWordSpec with Matchers with InitializeMDC with BeforeAndAfter {
  implicit val scheduler: Scheduler = Scheduler.global
  implicit val opts: Task.Options   = Task.defaultOptions.enableLocalContextPropagation

  val keyValue  = "key"
  val putValue  = "value"
  val key1Value = "key1"
  val put1Value = "value1"
  val key2Value = "key2"
  val put2Value = "value2"

  override def after(fun: => Any)(implicit pos: Position): Unit = {
    MDC.remove(keyValue)
    MDC.remove(key1Value)
    MDC.remove(key2Value)
  }

  def getAndPut(key: String, value: String): Task[String] =
    for {
      _ <- Task {
            MDC.put(key, value)
          }
      get <- Task {
              MDC.get(key)
            }
    } yield get

  "Task with MDC" can {
    "Write and get a value" in {
      val task = getAndPut(keyValue, putValue)
      task.runToFuture.map { _ shouldBe putValue }
    }

    "Write and get different values concurrently" in {
      val task = Task.gather(
        List(
          getAndPut(key1Value, put1Value).executeAsync,
          getAndPut(key2Value, put2Value).executeAsync
        ))

      task.runToFuture.map {
        case List(one, two) =>
          one shouldBe put1Value
          two shouldBe put2Value
      }
    }

    "Write and get different values concurrently and mixed" in {
      val task = Task.gather(
        List(
          getAndPut(keyValue, put1Value).executeAsync,
          getAndPut(keyValue, put2Value).executeAsync
        ))

      task.runToFuture.map {
        case List(one, two) =>
          one shouldBe put1Value
          two shouldBe put2Value
      }
    }

  }
}
