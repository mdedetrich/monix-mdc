import monix.eval.Task
import monix.execution.Scheduler
import org.scalatest.{AsyncWordSpec, Matchers}
import org.slf4j.MDC

import scala.concurrent.ExecutionContext

class MDCBasicSpec extends AsyncWordSpec with Matchers with InitializeMDC {
  implicit val scheduler: Scheduler               = Scheduler.global
  override def executionContext: ExecutionContext = scheduler
  implicit val opts: Task.Options                 = Task.defaultOptions.enableLocalContextPropagation

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
      val keyValue = KeyValue.keyValueGenerator.sample.get

      val task = getAndPut(keyValue.key, keyValue.value)
      task.runToFutureOpt.map { _ shouldBe keyValue.value }
    }

    "Write and get different values concurrently" in {
      val keyValues = MultipleKeysMultipleValues.multipleKeyValueGenerator.sample.get

      val tasks = keyValues.keysAndValues.map { keyValue =>
        getAndPut(keyValue.key, keyValue.value).executeAsync
      }

      val task = Task.gather(tasks)

      task.runToFutureOpt.map { retrievedKeyValues =>
        retrievedKeyValues.size shouldBe keyValues.keysAndValues.size
        retrievedKeyValues.toSet shouldBe keyValues.keysAndValues.map(_.value).toSet
      }
    }

    "Write and get different values concurrently and mixed" in {
      val keyMultipleValues = KeyMultipleValues.keyMultipleValuesGenerator.sample.get

      val tasks = keyMultipleValues.values.map { value =>
        getAndPut(keyMultipleValues.key, value).executeAsync
      }

      val task = Task.gather(tasks)

      task.runToFutureOpt.map { retrievedValues =>
        retrievedValues.size shouldBe keyMultipleValues.values.size
        retrievedValues.toSet shouldBe keyMultipleValues.values.toSet
      }
    }

  }
}
