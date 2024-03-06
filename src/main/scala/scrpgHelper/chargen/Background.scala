package scrpgHelper.chargen

import scrpgHelper.rolls.Die

final case class Background(
    name: String,
    number: Int,
    qualityList: List[Quality],
    mandatoryQualities: List[Quality],
    principleCategory: PrincipleCategory,
    backgroundDice: List[Die],
    powerSourceDice: List[Die]
):
  def valid(qualities: List[(Quality, Die)], abilities: List[Ability[_]]): Boolean =
      qualities.size == backgroundDice.size
        && mandatoryQualities.toSet.diff(qualities.map(_._1).toSet).isEmpty
        && !abilities.filter(_.inPrincipleCategory(principleCategory)).isEmpty
  end valid
end Background

object Background:
  import scrpgHelper.rolls.Die.d
  import Quality as Q
  import PrincipleCategory as PC

  def apply(
      name: String,
      number: Int,
      qualityList: List[Quality],
      principleCategory: PC,
      backgroundDice: List[Die],
      powerSourceDice: List[Die]
  ): Background =
    new Background(
      name,
      number,
      qualityList,
      List(),
      principleCategory,
      backgroundDice,
      powerSourceDice
    )
  end apply

  def apply(
      name: String,
      number: Int,
      qualityList: List[Quality],
      mandatoryQualities: List[Quality],
      principleCategory: PC,
      backgroundDice: List[Die],
      powerSourceDice: List[Die]
  ): Background =
    new Background(
      name,
      number,
      qualityList,
      mandatoryQualities,
      principleCategory,
      backgroundDice,
      powerSourceDice
    )
  end apply

  val upperClass: Background = Background(
    "Upper Class",
    1,
    List(Q.fitness, Q.persuasion) ++ Q.mentalQualities,
    PC.Responsibility,
    List(d(10), d(8)),
    List(d(10), d(8), d(8))
  )

  val blankSlate: Background = Background(
    "Blank Slate",
    2,
    Q.mentalQualities ++ Q.physicalQualities,
    PC.Identity,
    List(d(10), d(8)),
    List(d(10), d(8), d(8))
  )

  val struggling: Background = Background(
    "Struggling",
    3,
    List(Q.banter, Q.criminalUnderworldInfo) ++ Q.physicalQualities,
    PC.Responsibility,
    List(d(8), d(6), d(6)),
    List(d(8), d(8), d(6))
  )

  val adventurer: Background = Background(
    "Adventurer",
    4,
    List(Q.history, Q.leadership) ++ Q.physicalQualities,
    PC.Expertise,
    List(d(10), d(8)),
    List(d(8), d(8), d(8))
  )

  val unremarkable: Background = Background(
    "Unremarkable",
    5,
    List(Q.closeCombat) ++ Q.mentalQualities ++ Q.socialQualities,
    PC.Identity,
    List(d(10), d(8)),
    List(d(10), d(8), d(6))
  )

  val lawEnforcement: Background = Background(
    "Law Enforcement",
    6,
    List(
      Q.closeCombat,
      Q.criminalUnderworldInfo,
      Q.rangedCombat
    ) ++ Q.mentalQualities ++ Q.socialQualities,
    PC.Responsibility,
    List(d(10), d(8)),
    List(d(10), d(8), d(6))
  )

  val academic: Background = Background(
    "Academic",
    7,
    List(Q.leadership, Q.selfDiscipline) ++ Q.informationQualities,
    PC.Expertise,
    List(d(12), d(8)),
    List(d(10), d(8))
  )

  val tragic: Background = Background(
    "Tragic",
    8,
    List(Q.banter, Q.closeCombat, Q.imposing) ++ Q.mentalQualities,
    PC.Ideals,
    List(d(10), d(8)),
    List(d(10), d(10), d(6))
  )

  val performer: Background = Background(
    "Performer",
    9,
    List(Q.acrobatics, Q.creativity, Q.finesse) ++ Q.socialQualities,
    PC.Responsibility,
    List(d(10), d(8)),
    List(d(10), d(8), d(6))
  )

  val military: Background = Background(
    "Military",
    10,
    List(Q.leadership, Q.selfDiscipline) ++ Q.physicalQualities,
    PC.Ideals,
    List(d(10), d(8)),
    List(d(10), d(8), d(8))
  )

  val retired: Background = Background(
    "Retired",
    11,
    Q.informationQualities ++ Q.socialQualities,
    PC.Identity,
    List(d(10), d(10)),
    List(d(12), d(6), d(6))
  )

  val criminal: Background = Background(
    "Criminal",
    12,
    List(Q.criminalUnderworldInfo, Q.imposing) ++ Q.physicalQualities,
    PC.Expertise,
    List(d(10), d(8)),
    List(d(8), d(8), d(8))
  )

  val medical: Background = Background(
    "Medical",
    13,
    List(Q.finesse, Q.science, Q.technology) ++ Q.mentalQualities,
    List(Q.medicine),
    PC.Expertise,
    List(d(10), d(8), d(6)),
    List(d(10), d(8), d(8))
  )

  val anachronistic: Background = Background(
    "Anachronistic",
    14,
    List(Q.history, Q.magicalLore, Q.technology) ++ Q.physicalQualities,
    PC.Esoteric,
    List(d(10), d(8)),
    List(d(10), d(8), d(6))
  )

  val exile: Background = Background(
    "Exile",
    15,
    List(Q.conviction, Q.insight) ++ Q.informationQualities,
    PC.Ideals,
    List(d(10), d(8)),
    List(d(8), d(8), d(8))
  )

  val formerVillain: Background = Background(
    "Former Villain",
    16,
    List(Q.conviction) ++ Q.informationQualities ++ Q.socialQualities,
    PC.Expertise,
    List(d(10), d(8)),
    List(d(10), d(8), d(8))
  )

  val interstellar: Background = Background(
    "Interstellar",
    17,
    Q.informationQualities ++ Q.mentalQualities,
    PC.Esoteric,
    List(d(12), d(6)),
    List(d(10), d(8), d(6))
  )

  val dynasty: Background = Background(
    "Dynasty",
    18,
    List(Q.closeCombat, Q.fitness, Q.history) ++ Q.socialQualities,
    PC.Ideals,
    List(d(10), d(10)),
    List(d(8), d(8), d(6))
  )

  val otherworldly: Background = Background(
    "Otherworldly",
    19,
    List(Q.magicalLore, Q.otherworldlyMythos) ++ Q.mentalQualities,
    PC.Esoteric,
    List(d(10), d(8)),
    List(d(10), d(6), d(6))
  )

  val created: Background = Background(
    "Created",
    20,
    List(Q.alertness, Q.science, Q.technology) ++ Q.physicalQualities,
    PC.Expertise,
    List(d(12), d(6)),
    List(d(10), d(10), d(6))
  )

  val backgrounds: List[Background] = List(
    upperClass,
    blankSlate,
    struggling,
    adventurer,
    unremarkable,
    lawEnforcement,
    academic,
    tragic,
    performer,
    military,
    retired,
    criminal,
    medical,
    anachronistic,
    exile,
    formerVillain,
    interstellar,
    dynasty,
    otherworldly,
    created
  )
end Background
