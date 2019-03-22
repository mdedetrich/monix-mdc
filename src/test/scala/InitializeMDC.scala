import org.mdedetrich.monix.mdc.MonixMDCAdapter
import org.scalatest.{BeforeAndAfterAll, Suite}

trait InitializeMDC extends BeforeAndAfterAll { this: Suite =>
  override def beforeAll(): Unit =
    MonixMDCAdapter.initialize()
}
