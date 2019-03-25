import org.scalacheck.{Arbitrary, Gen}

final case class KeyValue(key: String, value: String)

object KeyValue {
  lazy val keyValueGenerator: Gen[KeyValue] = for {
    key   <- Gen.asciiStr
    value <- Gen.asciiStr
  } yield KeyValue(key, value)

  implicit val arbKeyValue: Arbitrary[KeyValue] = Arbitrary(keyValueGenerator)
}
