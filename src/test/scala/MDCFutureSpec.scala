import monix.eval.Task
import monix.execution.schedulers.TracingScheduler
import org.scalactic.source.Position
import org.scalatest.{AsyncWordSpec, BeforeAndAfter, Matchers}
import org.slf4j.MDC

import scala.concurrent.{ExecutionContext, Future}

class MDCFutureSpec extends AsyncWordSpec with Matchers with InitializeMDC with BeforeAndAfter {
  implicit val scheduler: TracingScheduler = TracingScheduler(ExecutionContext.global)
  implicit val opts: Task.Options          = Task.defaultOptions.enableLocalContextPropagation

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

  "Mixing Task with Future" can {
    "Write with Task and get in Future" in {
      val task = for {
        _ <- Task {
              MDC.put(keyValue, putValue)
            }
        get <- Task.fromFuture {
                Future {
                  MDC.get(keyValue)
                }
              }
      } yield get

      task.runToFutureOpt.map { _ shouldBe putValue }
    }

    "Write with Future and get in Task" in {
      val task = for {
        _ <- Task.deferFuture {
              Future {
                MDC.put(keyValue, putValue)
              }
            }
        get <- Task {
                MDC.get(keyValue)
              }
      } yield get
      task.runToFutureOpt.map { _ shouldBe putValue }
    }

    "Write and get different values concurrently and mixed with Future first" in {
      def getAndPut(key: String, value: String): Task[String] =
        for {
          _ <- Task.deferFuture {
                Future {
                  MDC.put(key, value)
                }
              }
          get <- Task {
                  MDC.get(key)
                }
        } yield get

      val task = Task.gather(
        List(
          getAndPut(keyValue, put1Value).executeAsync,
          getAndPut(keyValue, put2Value).executeAsync
        ))

      task.runToFutureOpt.map {
        case List(one, two) =>
          one shouldBe put1Value
          two shouldBe put2Value
      }
    }

    "Write and get different values concurrently and mixed with Future second" in {
      def getAndPut(key: String, value: String): Task[String] =
        for {
          _ <- Task {
                MDC.put(key, value)
              }
          get <- Task.deferFuture {
                  Future {
                    MDC.get(key)
                  }
                }
        } yield get

      val task = Task.gather(
        List(
          getAndPut(keyValue, put1Value).executeAsync,
          getAndPut(keyValue, put2Value).executeAsync
        ))

      task.runToFutureOpt.map {
        case List(one, two) =>
          one shouldBe put1Value
          two shouldBe put2Value
      }
    }
  }

}
