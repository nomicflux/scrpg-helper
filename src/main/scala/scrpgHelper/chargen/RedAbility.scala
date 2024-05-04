package scrpgHelper.chargen

import scrpgHelper.status.Status

case class RedAbility(
    abilityTemplate: AbilityTemplate,
    allowed: (List[Quality], List[Power]) => Boolean
)

object RedAbility:
  type RedAbilityPhase = Unit
  val redAbilityPhase: RedAbilityPhase = ()

  def allowedAbilityPool(qs: List[Quality], ps: List[Power]): AbilityPool =
      baseRedAbilityPool.copy(id = baseRedAbilityPool.id,
                              abilities = allRedAbilities
                                  .filter(_.allowed(qs, ps))
                                  .map(_.abilityTemplate))

  def apply(
      name: String,
      abilityCategory: AbilityCategory,
      actions: List[Action],
      description: Description,
      allowedCategory: PowerCategory | QualityCategory
  ): RedAbility =
    RedAbility(
      AbilityTemplate(
        name,
        Status.Red,
        abilityCategory,
        _ => actions,
        description
      ),
      allowedCategory match
        case pc: PowerCategory =>
          { (_qs, ps) =>
              //println(s"Powers: ${ps}")
              //println(s"Power Categories: ${ps.map(_.category).toSet}")
              //println(s"Needed Power Category: ${pc}")
              //println(s"Has Power Category: ${ps.map(_.category).toSet.contains(pc)}")
              ps.map(_.category).toSet.contains(pc)
          }
        case qc: QualityCategory =>
          { (qs, _ps) =>
              //println(s"Qualities: ${qs}")
              //println(s"Quality Categories: ${qs.map(_.category).toSet}")
              //println(s"Needed Quality Category: ${qc}")
              //println(s"Has Quality Category: ${qs.map(_.category).toSet.contains(qc)}")
              qs.map(_.category).toSet.contains(qc)
          }
    )

  def allowedRedAbilities(
      qualities: List[Quality],
      powers: List[Power]
  ): List[AbilityTemplate] =
    allRedAbilities.filter(_.allowed(qualities, powers)).map(_.abilityTemplate)

  val athleticAbility: RedAbility = RedAbility(
    "Athletic",
    AbilityCategory.Action,
    List(Action.Boost),
    List(
      "Ability Test",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Athletic)),
      "."
    ),
    PowerCategory.Athletic
  )

  val energyAbility: RedAbility = RedAbility(
    "Energy",
    AbilityCategory.Action,
    List(Action.Boost),
    List(
      "Ability Test",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Energy)),
      "."
    ),
    PowerCategory.Energy
  )

  val hallmarkAbility: RedAbility = RedAbility(
    "Hallmark",
    AbilityCategory.Action,
    List(Action.Boost),
    List(
      "Ability Test",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Hallmark)),
      "."
    ),
    PowerCategory.Hallmark
  )

  val intellectualAbility: RedAbility = RedAbility(
    "Intellectual",
    AbilityCategory.Action,
    List(Action.Boost),
    List(
      "Ability Test",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Intellectual)),
      "."
    ),
    PowerCategory.Intellectual
  )

  val materialAbility: RedAbility = RedAbility(
    "Material",
    AbilityCategory.Action,
    List(Action.Boost),
    List(
      "Ability Test",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Material)),
      "."
    ),
    PowerCategory.Material
  )

  val selfControlAbility: RedAbility = RedAbility(
    "SelfControl",
    AbilityCategory.Action,
    List(Action.Boost),
    List(
      "Ability Test",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.SelfControl)),
      "."
    ),
    PowerCategory.SelfControl
  )

  val psychicAbility: RedAbility = RedAbility(
    "Psychic",
    AbilityCategory.Action,
    List(Action.Boost),
    List(
      "Ability Test",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Psychic)),
      "."
    ),
    PowerCategory.Psychic
  )

  val mobilityAbility: RedAbility = RedAbility(
    "Mobility",
    AbilityCategory.Action,
    List(Action.Boost),
    List(
      "Ability Test",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Mobility)),
      "."
    ),
    PowerCategory.Mobility
  )

  val technologicalAbility: RedAbility = RedAbility(
    "Technological",
    AbilityCategory.Action,
    List(Action.Boost),
    List(
      "Ability Test",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Technological)),
      "."
    ),
    PowerCategory.Technological
  )

  val informationAbility: RedAbility = RedAbility(
    "Information",
    AbilityCategory.Action,
    List(Action.Boost),
    List(
      "Ability Test",
      QualityChoice(AbilityChoice.qualityCategory(QualityCategory.Information)),
      "."
    ),
    QualityCategory.Information
  )

  val mentalAbility: RedAbility = RedAbility(
    "Mental",
    AbilityCategory.Action,
    List(Action.Boost),
    List(
      "Ability Test",
      QualityChoice(AbilityChoice.qualityCategory(QualityCategory.Mental)),
      "."
    ),
    QualityCategory.Mental
  )

  val physicalAbility: RedAbility = RedAbility(
    "Physical",
    AbilityCategory.Action,
    List(Action.Boost),
    List(
      "Ability Test",
      QualityChoice(AbilityChoice.qualityCategory(QualityCategory.Physical)),
      "."
    ),
    QualityCategory.Physical
  )

  val socialAbility: RedAbility = RedAbility(
    "Social",
    AbilityCategory.Action,
    List(Action.Boost),
    List(
      "Ability Test",
      QualityChoice(AbilityChoice.qualityCategory(QualityCategory.Social)),
      "."
    ),
    QualityCategory.Social
  )

  val allRedAbilities: List[RedAbility] = List(
    athleticAbility,
    energyAbility,
    hallmarkAbility,
    intellectualAbility,
    materialAbility,
    selfControlAbility,
    psychicAbility,
    mobilityAbility,
    technologicalAbility,
    informationAbility,
    mentalAbility,
    physicalAbility,
    socialAbility
  )

  val redAbilityLookup: Map[AbilityId, RedAbility] =
      allRedAbilities.map(a => a.abilityTemplate.id -> a).toMap

  val baseRedAbilityPool: AbilityPool = AbilityPool(
    2,
    allRedAbilities.map(_.abilityTemplate)
  )
end RedAbility
