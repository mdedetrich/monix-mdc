import org.scalacheck.{Arbitrary, Gen}

final case class KeyMultipleValues(key: String, values: List[String])

object KeyMultipleValues {
  lazy val keyMultipleValuesGenerator: Gen[KeyMultipleValues] = for {
    key    <- Gen.asciiStr
    range  <- Gen.chooseNum(2, 10)
    values <- Gen.listOfN(range, Gen.alphaStr)
  } yield KeyMultipleValues(key, values)

  implicit val arbKeyMultipleValues: Arbitrary[KeyMultipleValues] = Arbitrary(keyMultipleValuesGenerator)
}
