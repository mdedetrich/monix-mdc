import monix.eval.Task
import monix.execution.misc.Local
import monix.execution.schedulers.TracingScheduler
import org.scalatest.BeforeAndAfter
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AsyncWordSpec
import org.slf4j.MDC

import scala.concurrent.{ExecutionContext, Future}

class MDCFutureSpec extends AsyncWordSpec with Matchers with InitializeMDC with BeforeAndAfter {
  implicit val scheduler: TracingScheduler        = TracingScheduler(ExecutionContext.global)
  override def executionContext: ExecutionContext = scheduler
  implicit val opts: Task.Options                 = Task.defaultOptions.enableLocalContextPropagation

  before {
    MDC.clear()
  }

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

      task.runToFutureOpt.map(_ shouldBe keyValue.value)
    }

    "Write with Task and get in Future inside Future for comprehension" in {
      val keyValue = KeyValue.keyValueGenerator.sample.get

      val future = for {
        _ <- Task {
               MDC.put(keyValue.key, keyValue.value)
             }.runToFutureOpt
        get <- Future {
                 MDC.get(keyValue.key)
               }
      } yield get

      future.map(_ shouldBe keyValue.value)
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

      task.runToFutureOpt.map(_ shouldBe keyValue.value)
    }
  }

  def getAndPut(key: String, value: String): Future[String] =
    for {
      _ <- Future {
             MDC.put(key, value)
           }
      get <- Future {
               MDC.get(key)
             }
    } yield get

  "Using Future only" can {
    "Write and get a value" in {
      val keyValue = KeyValue.keyValueGenerator.sample.get

      val future = getAndPut(keyValue.key, keyValue.value)

      future.map(_ shouldBe keyValue.value)
    }

    "Write and get different values concurrently" in {
      val keyValues = MultipleKeysMultipleValues.multipleKeyValueGenerator.sample.get

      val futures = keyValues.keysAndValues.map { keyValue =>
        Local.isolate(getAndPut(keyValue.key, keyValue.value))
      }

      val future = Future.sequence(futures)

      future.map { retrievedKeyValues =>
        retrievedKeyValues.size shouldBe keyValues.keysAndValues.size
        retrievedKeyValues shouldBe keyValues.keysAndValues.map(_.value)
      }
    }
  }

}
