package scrpgHelper.chargen

import scrpgHelper.rolls.Die
import scrpgHelper.status.Status

case class Personality(
    number: Int,
    name: String,
    outAbilityPool: AbilityPool,
    statusDice: Map[Status, Die]
):
  def valid(
      qualities: List[Quality],
      abilities: List[ChosenAbility]
  ): Boolean =
    qualities.map(_.name != "").foldLeft(true)(_ && _) &&
        abilities.map(_.valid).foldLeft(true)(_ && _)
end Personality

object Personality:
  import scrpgHelper.rolls.Die.d

  def apply(
      number: Int,
      name: String,
      outAbilityText: Description,
      statusDice: List[Die]
  ): Personality =
    new Personality(
      number,
      name,
      AbilityPool(
        1,
        List(AbilityTemplate(
          s"$name Out Ability",
          Status.Out,
          AbilityCategory.Action,
          _ => List(),
          outAbilityText
        ))
      ),
      statusDice
        .zip(List(Status.Green, Status.Yellow, Status.Red))
        .map { case (a, b) => (b, a) }
        .toMap
    )
  val loneWolf = Personality(
    1,
    "Lone Wolf",
    List(
      "Boost an ally by rolling your single",
      QualityChoice(),
      "die."
    ),
    List(d(8), d(8), d(8))
  )
  val naturalLeader = Personality(
    2,
    "Natural Leader",
    List(
      "Boost an ally by rolling your single",
      QualityChoice(),
      "die."
    ),
    List(d(6), d(8), d(10))
  )
  val sarcastic = Personality(
    5,
    "Sarcastic",
    List(
      "Hinder an opponent by rolling your single",
      QualityChoice(),
      "die."
    ),
    List(d(8), d(8), d(8))
  )
  val distant = Personality(
    6,
    "Distant",
    List(
      "Boost an ally by rolling your single Red status die."
    ),
    List(d(10), d(8), d(6))
  )
  val stalwart = Personality(
    7,
    "Stalwart",
    List(
      "Defend an ally by rolling your single",
      PowerChoice(),
      "die."
    ),
    List(d(8), d(8), d(8))
  )
  val fastTalking = Personality(
    8,
    "Fast Talking",
    List(
      "Hinder a minion or lieutenant by rolling your single",
      QualityChoice(),
      "die, and increase that penalty by -1."
    ),
    List(d(6), d(8), d(10))
  )
  val inquisitive = Personality(
    9,
    "Inquisitive",
    List(
      "Choose an ally. Until your next turn, that ally may reroll one of their dice by using a Reaction."
    ),
    List(d(6), d(8), d(10))
  )
  val alluring = Personality(
    10,
    "Alluring",
    List(
      "Boost an ally by rolling your single",
      PowerChoice(),
      "die."
    ),
    List(d(6), d(8), d(10))
  )
  val stoic = Personality(
    11,
    "Stoic",
    List(
      "Defend an ally by rolling your single",
      QualityChoice(),
      "die."
    ),
    List(d(6), d(8), d(10))
  )
  val nurturing = Personality(
    12,
    "Nurturing",
    List(
      "Boost an ally by rolling your single",
      QualityChoice(),
      "die."
    ),
    List(d(6), d(6), d(12))
  )
  val analytical = Personality(
    13,
    "Analytical",
    List(
      "Remove a bonus or penalty of your choice."
    ),
    List(d(10), d(8), d(6))
  )
  val decisive = Personality(
    14,
    "Decisive",
    List(
      "Boost an ally by rolling your single",
      PowerChoice(),
      "die."
    ),
    List(d(8), d(8), d(8))
  )
  val jovial = Personality(
    15,
    "Jovial",
    List(
      "Defend an ally by rolling your single",
      QualityChoice(),
      "die."
    ),
    List(d(6), d(8), d(10))
  )
  val cheerful = Personality(
    16,
    "Cheerful",
    List(
      "Boost an ally by rolling your single",
      PowerChoice(),
      "die."
    ),
    List(d(10), d(8), d(6))
  )
  val naive = Personality(
    17,
    "Naive",
    List(
      "Hinder an opponent by rolling your single",
      PowerChoice(),
      "die."
    ),
    List(d(6), d(6), d(12))
  )
  val apathetic = Personality(
    18,
    "Apathetic",
    List(
      "Remove a bonus or penalty of your choice."
    ),
    List(d(6), d(8), d(10))
  )
  val jaded = Personality(
    19,
    "Jaded",
    List(
      "Remove a bonus or penalty of your choice."
    ),
    List(d(10), d(8), d(6))
  )
  val arrogant = Personality(
    20,
    "Arrogant",
    List(
      "Hinder an opponent by rolling your single",
      PowerChoice(),
      "die."
    ),
    List(d(10), d(8), d(6))
  )

  val personalities: List[Personality] = List(
    loneWolf,
    naturalLeader,
    // impulsive, // TODO: add upgrade
    // mischievous, // TODO: change health qualities
    sarcastic,
    distant,
    stalwart,
    fastTalking,
    inquisitive,
    alluring,
    stoic,
    nurturing,
    analytical,
    decisive,
    jovial,
    cheerful,
    naive,
    apathetic,
    jaded,
    arrogant
  )
end Personality
