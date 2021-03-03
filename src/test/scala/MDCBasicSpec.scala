import monix.eval.{Task, TaskLocal}
import monix.execution.Scheduler
import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.slf4j.MDC

import scala.concurrent.ExecutionContext

class MDCBasicSpec extends AsyncWordSpec with Matchers with InitializeMDC with BeforeAndAfter {
  implicit val scheduler: Scheduler               = Scheduler.global
  override def executionContext: ExecutionContext = scheduler
  implicit val opts: Task.Options                 = Task.defaultOptions.enableLocalContextPropagation

  before {
    MDC.clear()
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
      val keyValue = KeyValue.keyValueGenerator.sample.get

      val task = getAndPut(keyValue.key, keyValue.value)
      task.runToFutureOpt.map(_ shouldBe keyValue.value)
    }

    "Write and get different values concurrently" in {
      val keyValues = MultipleKeysMultipleValues.multipleKeyValueGenerator.sample.get

      val tasks = keyValues.keysAndValues.map { keyValue =>
        TaskLocal.isolate(getAndPut(keyValue.key, keyValue.value).executeAsync)
      }

      val task = Task.parSequence(tasks)

      task.runToFutureOpt.map { retrievedKeyValues =>
        retrievedKeyValues.size shouldBe keyValues.keysAndValues.size
        retrievedKeyValues shouldBe keyValues.keysAndValues.map(_.value)
      }
    }

  }
}
