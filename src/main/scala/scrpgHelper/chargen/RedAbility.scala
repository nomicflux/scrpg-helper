package scrpgHelper.chargen

import scrpgHelper.status.Status

case class RedAbility(
    abilityTemplate: AbilityTemplate,
    allowed: (List[Quality], List[Power]) => Boolean
)

object RedAbility:
  type RedAbilityPhase = Unit
  val redAbilityPhase: RedAbilityPhase = ()

  val baseRedAbilityPool: AbilityPool = AbilityPool(
    2,
    allRedAbilities.map(_.abilityTemplate)
  )

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
          (_qs, ps) => ps.map(_.category).toSet.contains(pc)
        case qc: QualityCategory =>
          (qs, _ps) => qs.map(_.category).toSet.contains(qc)
    )

  def allowedRedAbilities(
      qualities: List[Quality],
      powers: List[Power]
  ): List[AbilityTemplate] =
    allRedAbilities.filter(_.allowed(qualities, powers)).map(_.abilityTemplate)

  def athleticAbility: RedAbility = RedAbility(
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

  def energyAbility: RedAbility = RedAbility(
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

  def hallmarkAbility: RedAbility = RedAbility(
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

  def intellectualAbility: RedAbility = RedAbility(
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

  def materialAbility: RedAbility = RedAbility(
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

  def selfControlAbility: RedAbility = RedAbility(
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

  def psychicAbility: RedAbility = RedAbility(
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

  def mobilityAbility: RedAbility = RedAbility(
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

  def technologicalAbility: RedAbility = RedAbility(
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

  def informationAbility: RedAbility = RedAbility(
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

  def mentalAbility: RedAbility = RedAbility(
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

  def physicalAbility: RedAbility = RedAbility(
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

  def socialAbility: RedAbility = RedAbility(
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

  def allRedAbilities: List[RedAbility] = List(
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
end RedAbility
