package scrpgHelper.rolls

enum EffectDieType:
    case Min, Mid, Max

    def getEffect(n: Int, m: Int, l: Int): Int = this match
        case Min => Seq(n, m, l).min
        case Mid => Seq(n, m, l).sortWith(_ < _).drop(1).head
        case Max => Seq(n, m, l).max
    end getEffect
end EffectDieType

case class Die(n: Int):
    def values: Range = 1 to n
end Die

object Die:
    def apply(n: Int): Die =
      new Die(n)

    def d(n: Int): Die =
      apply(n)

    def cartesian(d1: Die, d2: Die, d3: Die): Seq[(Int, Int, Int)] =
      for n <- d1.values
          m <- d2.values
          l <- d3.values
      yield (n, m, l)
    end cartesian

    def results(d1: Die, d2: Die, d3: Die, effects: Seq[EffectDieType]): Seq[Int] =
      for n <- d1.values
          m <- d2.values
          l <- d3.values
      yield effects.map(_.getEffect(n, m, l)).sum
    end results

    def freqs(d1: Die, d2: Die, d3: Die, effects: Seq[EffectDieType]): Map[Int, Int] =
      results(d1, d2, d3, effects).groupBy(identity).map( (k,v) => (k, v.size) )
    end freqs
end Die
