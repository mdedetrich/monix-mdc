import monix.eval.Task
import monix.execution.schedulers.TracingScheduler
import org.scalatest.{AsyncWordSpec, Matchers}
import org.slf4j.MDC

import scala.concurrent.{ExecutionContext, Future}

class MDCFutureSpec extends AsyncWordSpec with Matchers with InitializeMDC {
  implicit val scheduler: TracingScheduler        = TracingScheduler(ExecutionContext.global)
  override def executionContext: ExecutionContext = scheduler
  implicit val opts: Task.Options                 = Task.defaultOptions.enableLocalContextPropagation

  "Mixing Task with Future" can {
    "Write with Task and get in Future" in {
      val keyValue = KeyValue.keyValueGenerator.sample.get

      val task = for {
        _ <- Task {
              MDC.put(keyValue.key, keyValue.value)
            }
        get <- Task.fromFuture {
                Future {
                  MDC.get(keyValue.key)
                }
              }
      } yield get

      task.runToFutureOpt.map { _ shouldBe keyValue.value }
    }

    "Write with Future and get in Task" in {
      val keyValue = KeyValue.keyValueGenerator.sample.get

      val task = for {
        _ <- Task.deferFuture {
              Future {
                MDC.put(keyValue.key, keyValue.value)
              }
            }
        get <- Task {
                MDC.get(keyValue.key)
              }
      } yield get

      task.runToFutureOpt.map { _ shouldBe keyValue.value }
    }

    "Write and get different values concurrently and mixed with Future first" in {
      val keyMultipleValues = KeyMultipleValues.keyMultipleValuesGenerator.sample.get

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

      val tasks = keyMultipleValues.values.map { value =>
        getAndPut(keyMultipleValues.key, value).executeAsync
      }

      val task = Task.gather(tasks)

      task.runToFutureOpt.map { retrievedValues =>
        retrievedValues shouldBe keyMultipleValues.values
      }
    }

    "Write and get different values concurrently and mixed with Future second" in {
      val keyMultipleValues = KeyMultipleValues.keyMultipleValuesGenerator.sample.get

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

      val tasks = keyMultipleValues.values.map { value =>
        getAndPut(keyMultipleValues.key, value).executeAsync
      }

      val task = Task.gather(tasks)

      task.runToFutureOpt.map { retrievedValues =>
        retrievedValues shouldBe keyMultipleValues.values
      }
    }
  }

}
