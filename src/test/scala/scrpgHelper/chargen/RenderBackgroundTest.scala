package scrpgHelper.chargen

class RenderBackgroundTest extends munit.FunSuite:
  import scrpgHelper.rolls.Die.d

  test("enforces mandatory qualities") {
    val check = RenderBackground.mandatoryQualityCheck(Background.medical)
    val l1 = (Quality.science, d(8))
    val l2 = (Quality.technology, d(10))
    val l3 = (Quality.finesse, d(8))
    val l4 = (Quality.medicine, d(8))
    assert(check(List())(l1, None))
    assert(check(List())(l4, None))
    assert(check(List(l1))(l2, None))
    assert(check(List(l1))(l4, None))
    assert(!check(List(l1, l2))(l3, None))
    assert(check(List(l1, l2))(l4, None))
    assert(check(List(l1, l2, l4))(l3, Some(l2)))
    assert(!check(List(l1, l2, l4))(l3, Some(l4)))
    assert(check(List(l1, l2, l4))(l4, Some(l4)))
    assert(check(List(l1, l4))(l3, Some(l4)))
    assert(check(List(l1, l3))(l2, Some(l3)))
    assert(check(List(l1, l3))(l4, Some(l2)))
    assert(!check(List(l1, l3))(l2, Some(l2)))
  }
end RenderBackgroundTest
