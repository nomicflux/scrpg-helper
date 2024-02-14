package scrpgHelper

class DieTest extends munit.FunSuite:
    import Die.*
    import EffectDieType.*

    test("DieHasProperRange") {
      val d6 = d(6)
      assert(d6.values.start == 1)
      assert(d6.values.end == 6)

      val d12 = d(12)
      assert(d12.values.start == 1)
      assert(d12.values.end == 12)
    }

    test("CartesianProducts") {
      val product = cartesian(d(4), d(3), d(2))
      assert(product.toList == List((1,1,1), (1,1,2),
                                    (1,2,1), (1,2,2),
                                    (1,3,1), (1,3,2),
                                    (2,1,1), (2,1,2),
                                    (2,2,1), (2,2,2),
                                    (2,3,1), (2,3,2),
                                    (3,1,1), (3,1,2),
                                    (3,2,1), (3,2,2),
                                    (3,3,1), (3,3,2),
                                    (4,1,1), (4,1,2),
                                    (4,2,1), (4,2,2),
                                    (4,3,1), (4,3,2)))
    }

    test("EffectDieType") {
      assert(Min.getEffect(1,3,2) == 1)
      assert(Mid.getEffect(1,3,2) == 2)
      assert(Max.getEffect(1,3,2) == 3)

      assert(Min.getEffect(1,1,1) == 1)
      assert(Mid.getEffect(1,1,1) == 1)
      assert(Max.getEffect(1,1,1) == 1)

      assert(Min.getEffect(2,1,2) == 1)
      assert(Mid.getEffect(2,1,2) == 2)
      assert(Max.getEffect(2,1,2) == 2)

      assert(Min.getEffect(2,1,1) == 1)
      assert(Mid.getEffect(2,1,1) == 1)
      assert(Max.getEffect(2,1,1) == 2)
    }

    test("GeneratesResults") {
      assert(results(d(2), d(3), d(4), Seq(Mid)) ==
               Seq(1, 1, 1, 1, 1, 2, 2, 2, 1, 2, 3, 3,
                   1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3))
      assert(results(d(2), d(3), d(4), Seq(Min, Max)) ==
               Seq(2, 3, 4, 5, 3, 3, 4, 5, 4, 4, 4, 5,
                   3, 3, 4, 5, 3, 4, 5, 6, 4, 5, 5, 6))
    }

    test("GeneratesFrequencies") {
      assert(freqs(d(2), d(3), d(4), Seq(Mid)) ==
                Map(1 -> 7, 2 -> 13, 3 -> 4))
      assert(freqs(d(2), d(3), d(4), Seq(Min, Max)) ==
                Map(2 -> 1, 3 -> 6, 4 -> 8, 5 -> 7, 6 -> 2))
    }
end DieTest
