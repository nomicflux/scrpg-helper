package scrpgHelper.chargen

import scrpgHelper.status.Status
import scrpgHelper.rolls.Die

case class RedAbility(
    abilityTemplate: AbilityTemplate,
    allowed: (List[Quality], List[Power]) => Boolean
)

object RedAbility:
  type RedAbilityPhase = Unit
  val redAbilityPhase: RedAbilityPhase = ()

  def allowedAbilityPool(qs: List[Quality], ps: List[Power]): AbilityPool =
    baseRedAbilityPool.copy(
      id = baseRedAbilityPool.id,
      abilities = allRedAbilities
        .filter(_.allowed(qs, ps))
        .map(_.abilityTemplate)
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

  def withRestriction(
      name: String,
      abilityCategory: AbilityCategory,
      actions: List[Action],
      description: Description,
      restriction: (List[Quality], List[Power]) => Boolean
  ): RedAbility =
    RedAbility(
      AbilityTemplate(
        name,
        Status.Red,
        abilityCategory,
        _ => actions,
        description
      ),
      restriction
    )

  def allowedRedAbilities(
      qualities: List[Quality],
      powers: List[Power]
  ): List[AbilityTemplate] =
    allRedAbilities.filter(_.allowed(qualities, powers)).map(_.abilityTemplate)

  val majorRegeneration: RedAbility = RedAbility.withRestriction(
    "Major Regeneration",
    AbilityCategory.Action,
    List(Action.Hinder, Action.Recover),
    List(
      "Hinder yourself using",
      PowerChoice(AbilityChoice.onePower(Power.vitality)),
      ". Use your Min die. Recover health equal to you Max+Mid dice."
    ),
    (_, ps) => ps.contains(Power.vitality)
  )

  val paragonFeat: RedAbility = RedAbility(
    "Paragon Feat",
    AbilityCategory.Action,
    List(Action.Overcome, Action.Boost),
    List(
      "Overcome using",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Athletic)),
      "in a situation that requires you to be more than humanly capable, like an extreme feat of strength or speed. Use your Max+Min dice. Boost all reaby allies with your Mid die."
    ),
    PowerCategory.Athletic
  )

  val pushYourLimits: RedAbility = RedAbility(
    "Push Your Limits",
    AbilityCategory.Inherent,
    List(),
    List(
      "You have no limit on amount of Reactions you can take. Each time you use a Reaction after the first one each turn, take 1 irreducible damage or take a minor twist."
    ),
    PowerCategory.Athletic
  )

  val reactiveStrike: RedAbility = RedAbility(
    "Reactive Strike",
    AbilityCategory.Reaction,
    List(Action.Attack),
    List(
      "When you are Attacked and dealt damage, you may Attack the source of that damage by rolling your single",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Athletic)),
      "die, plus the amount of damage you take"
    ),
    PowerCategory.Athletic
  )

  val athleticPowerAbilities: List[RedAbility] = List(
    majorRegeneration,
    paragonFeat,
    pushYourLimits,
    reactiveStrike
  )

  val chargedUpBlastEnergy: RedAbility = RedAbility(
    "Charged Up Blast",
    AbilityCategory.Action,
    List(Action.Attack),
    List(
      "Attack using",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Energy)),
      "and at least one bonus. Use your Max+Mid+Min dice. Destroy all of your bonuses, adding each of them to this Attack first, even if they are exclusive."
    ),
    PowerCategory.Energy
  )

   val eruption: RedAbility = RedAbility(
    "Eruption",
    AbilityCategory.Action,
    List(Action.Attack),
    List(
      "Attack up to three targets, one of which must be you, using",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Energy)),
      ". Assign your Min, Mid, and Max dice as you choose among those targets."
    ),
    PowerCategory.Energy
  )

   val improvedImmunity: RedAbility = RedAbility(
    "Improved Immunity",
    AbilityCategory.Inherent,
    List(Action.Recover, Action.Boost),
    List(
      "If you would take damage from",
      EnergyChoice(),
      ", ignore that damage and Recover that amount instead. Use the value of the damage to Boost yourself."
    ),
    PowerCategory.Energy
  )

  val powerfulStrike: RedAbility = RedAbility(
    "Powerful Strike",
    AbilityCategory.Action,
    List(Action.Attack),
    List(
      "Attack using",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Energy)),
      ". Use your Max+Mid dice."
    ),
    PowerCategory.Energy
  )

  val purification: RedAbility = RedAbility(
    "Purification",
    AbilityCategory.Action,
    List(),
    List(
      "Remove all bonuses and penalties from the scene. You cannot use this ability again this scene."
    ),
    PowerCategory.Energy
  )

  val summonedAllies: RedAbility = RedAbility(
    "Summoned Allies",
    AbilityCategory.Action,
    List(),
    List(
      "Use",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Energy)),
      s". to create a number of ${Die.d(6)} minions equal to your Mid die. Choose the one same basic action that they each perform. They all act at the start of your turn."
    ),
    PowerCategory.Energy
  )

  val energyPowerAbilities: List[RedAbility] = List(
      chargedUpBlastEnergy,
      eruption,
      improvedImmunity,
      powerfulStrike,
      purification,
      summonedAllies,
  )

  val chargedUpBlastHallmark: RedAbility = RedAbility.withRestriction(
    "Charged Up Blast",
    AbilityCategory.Action,
    List(Action.Attack),
    List(
      "Attack using",
      PowerChoice(AbilityChoice.onePower(Power.signatureWeapon)),
      "and at least one bonus. Use your Max+Mid+Min dice. Destroy all of your bonuses, adding each of them to this Attack first, even if they are exclusive."
    ),
    (_qs, ps) => ps.contains(Power.signatureWeapon)
  )

  val quickExit: RedAbility = RedAbility.withRestriction(
    "Quick Exit",
    AbilityCategory.Action,
    List(Action.Attack, Action.Hinder),
    List(
      "Attack using",
      PowerChoice(AbilityChoice.onePower(Power.signatureVehicle)),
      ". Use your Max die. Hinder each nearby opponent with your Mid die. After using this ability, you and up to 2 allies may end up anywhere in the scene, even outside of the action."
    ),
    (_qs, ps) => ps.contains(Power.signatureVehicle)
  )

  val sacrificialRam: RedAbility = RedAbility.withRestriction(
    "Sacrificial Ram",
    AbilityCategory.Action,
    List(Action.Attack),
    List(
      "Attack up to three nearby targets using",
      PowerChoice(AbilityChoice.onePower(Power.signatureVehicle)),
      ". Use your Max+Mid dice against each of them. You cannot use your Signature Vehicle power for the rest of this scene and until it is recovered/repaired."
    ),
    (_qs, ps) => ps.contains(Power.signatureVehicle)
  )

  val ultimateWeaponry: RedAbility = RedAbility(
    "Ultimate Weaponry",
    AbilityCategory.Action,
    List(Action.Boost, Action.Attack),
    List(
      "Boost yourself using",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Hallmark)),
      ". Use your Max die. That bonus is persistent and exclusive. Attack using your Mid die plus that bonus."
    ),
    PowerCategory.Hallmark
  )

  val hallmarkPowerAbilities: List[RedAbility] = List(
      chargedUpBlastHallmark,
      quickExit,
      sacrificialRam,
      ultimateWeaponry,
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

  val allRedAbilities: List[RedAbility] =
    athleticPowerAbilities ++
    energyPowerAbilities ++
    hallmarkPowerAbilities ++
      List(
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
