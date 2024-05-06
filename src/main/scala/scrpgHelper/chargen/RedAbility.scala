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

  def apply(
      name: String,
      abilityCategory: AbilityCategory,
      actions: List[Action],
      description: Description,
      allowedCategory: PowerCategory | QualityCategory
  ): RedAbility =
    withRestriction(
      name,
      abilityCategory,
      actions,
      description,
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

  val majorRegenerationAthletic: RedAbility = RedAbility.withRestriction(
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
    majorRegenerationAthletic,
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

  val powerfulStrikeEnergy: RedAbility = RedAbility(
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

  val purificationEnergy: RedAbility = RedAbility(
    "Purification",
    AbilityCategory.Action,
    List(),
    List(
      "Remove all bonuses and penalties from the scene. You cannot use this ability again this scene."
    ),
    PowerCategory.Energy
  )

  val summonedAlliesEnergy: RedAbility = RedAbility(
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
    powerfulStrikeEnergy,
    purificationEnergy,
    summonedAlliesEnergy
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

  val ultimateWeaponryHallmark: RedAbility = RedAbility(
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
    ultimateWeaponryHallmark
  )

  val calculatedDodgeIntellectual: RedAbility = RedAbility(
    "Calculated Dodge",
    AbilityCategory.Reaction,
    List(),
    List(
      "You may take 1 irreducible damage to reroll the dice pool of a target that is Attacking or Hindering you.",
    ),
    PowerCategory.Intellectual
  )

   val giveTimeIntellectual: RedAbility = RedAbility(
    "Give Time",
    AbilityCategory.Action,
    List(Action.Boost),
    List(
      "Book another hero using",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Intellectual)),
      ". If that hero has already acted for the turn, use your Max die, and that hero loses Health equal to your Min die. That hero acts next in the turn order."
    ),
    PowerCategory.Intellectual
  )

   val reliableAptitudeIntellectual: RedAbility = RedAbility(
    "Reliable Aptitude",
    AbilityCategory.Inherent,
    List(),
    List(
      "When taking any action using",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Intellectual)),
      ", you may reroll your Min die before determining effects."
    ),
    PowerCategory.Intellectual
  )

  val unerringStrike: RedAbility = RedAbility(
    "Unerring Strike",
    AbilityCategory.Action,
    List(Action.Attack),
    List(
      "Attack using",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Intellectual)),
      ". Use your Max+Min dice. Ignore all penalties on this attack, ignore any Defend actions, and it cannot be affected by Reactions."
    ),
    PowerCategory.Intellectual
  )

  val intellectualPowerAbilities: List[RedAbility] = List(
      calculatedDodgeIntellectual,
      giveTimeIntellectual,
      reliableAptitudeIntellectual,
      unerringStrike,
  )

  val fieldOfHazards: RedAbility = RedAbility(
    "Field of Hazards",
    AbilityCategory.Action,
    List(Action.Hinder, Action.Attack),
    List(
      "Hinder any nummer of targets in the scene using",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Material)),
      ". Use your Max+Min dice. If you roll doubles, also Attack each target using your Mid die."
    ),
    PowerCategory.Material
  )

   val impenetrableDefenseMaterials: RedAbility = RedAbility(
    "Impenetrable Defense",
    AbilityCategory.Action,
    List(Action.Defend),
    List(
      "Defend using",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Material)),
      "against all Attacks against you until your next turn using your Max+Mid dice."
    ),
    PowerCategory.Material
  )

   val likeTheWind: RedAbility = RedAbility(
    "Like the Wind",
    AbilityCategory.Reaction,
    List(),
    List(
      "When you are Attacked and dealt damage, you may ignore that damage completely. If you do, treat the value of the damage as a Hinder action against you instead.",
    ),
    PowerCategory.Material
  )

   val powerfulStrikeMaterials: RedAbility = RedAbility(
    "Powerful Strike",
    AbilityCategory.Action,
    List(Action.Attack),
    List(
      "Attack using",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Material)),
      ". Use your Max+Mid dice."
    ),
    PowerCategory.Material
  )

   val summonedAlliesMaterials: RedAbility = RedAbility(
    "Summoned Allies",
    AbilityCategory.Action,
    List(),
    List(
      "Use",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Material)),
      s"to create a number of ${Die.d(6)} minions equal to your Mid die. Choose the one same basic action that they each perform. They all act at the start of your turn."
    ),
    PowerCategory.Material
  )

  val materialsPowerAbilities: List[RedAbility] = List(
      fieldOfHazards,
      impenetrableDefenseMaterials,
      likeTheWind,
      powerfulStrikeMaterials,
      summonedAlliesMaterials,
  )

   val calculatedDodgeMobility: RedAbility = RedAbility(
    "Calculated Dodge",
    AbilityCategory.Reaction,
    List(),
    List(
      "You may take 1 irreducible damage to reroll the dice pool of a target that is Attacking or Hindering you.",
    ),
    PowerCategory.Mobility
  )

   val heroicInterruption: RedAbility = RedAbility(
    "Heroic Interruption",
    AbilityCategory.Reaction,
    List(),
    List(
      s"When an Attack deals damage to a nearby hero in the Red zone, you may take ${Die.d(6)} irreducible damage to redirect that Attack to a target of your choice, other than the source of the Attack.",
    ),
    PowerCategory.Mobility
  )

   val intercession: RedAbility = RedAbility(
    "Intercession",
    AbilityCategory.Reaction,
    List(Action.Defend),
    List(
      "When multiple nearby heroes are Attack, you may take all the damage instead. If you do, roll your",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Mobility)),
      "die + Red zone die and Defend against the Attack by the total."
    ),
    PowerCategory.Mobility
  )

   val takeDown: RedAbility = RedAbility(
    "Take Down",
    AbilityCategory.Action,
    List(Action.Attack, Action.Hinder),
    List(
      "Attack using",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Mobility)),
      ". Use your Max die. Then, Hinder that target using your Mid+Min die."
    ),
    PowerCategory.Mobility
  )

   val untouchableMovement: RedAbility = RedAbility(
    "Untouchable Movement",
    AbilityCategory.Action,
    List(Action.Boost),
    List(
      "Boost yourself using",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Mobility)),
      ". Use your Max+Mid dice. Then, you may end up anywhere else in the scene, avoiding any dangers between your starting and ending locations."
    ),
    PowerCategory.Mobility
  )

  val mobilityPowerAbilities: List[RedAbility] = List(
      calculatedDodgeMobility,
      heroicInterruption,
      intercession,
      takeDown,
      untouchableMovement,
  )

   val dangerousHinder: RedAbility = RedAbility(
    "Dangerous Hinder",
    AbilityCategory.Action,
    List(Action.Hinder, Action.Attack),
    List(
      "Hinder using",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Psychic)),
      ". Use your Max+Mid dice. If you roll doubles, also Attack the target using your Mid+Min dice and take damage equal to you Min die."
    ),
    PowerCategory.Psychic
  )

   val direControl: RedAbility = RedAbility(
    "Dire Control",
    AbilityCategory.Action,
    List(),
    List(
      "Select a minion. That minion is now entirely under your control and acts at the start of your turn. If you are incapacitated, you lose control of this minion. You may also choose to release control of this minion at any time. At the end of the scene, this minion is defeated.",
    ),
    PowerCategory.Psychic
  )

   val finalWrathPsychic: RedAbility = RedAbility(
    "Final Wrath",
    AbilityCategory.Action,
    List(Action.Attack),
    List(
      "Attack using",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Psychic)),
      ". Use your Max+Mid+Min dice. Take a major twist."
    ),
    PowerCategory.Psychic
  )

   val giveTimePsychic: RedAbility = RedAbility(
    "Give Time",
    AbilityCategory.Action,
    List(Action.Boost),
    List(
      "Boost another hero using",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Psychic)),
      ". If that hero has already acted for the turn, use your Max die, and that hero loses health equal to your Min die. That hero acts next in the turn order."
    ),
    PowerCategory.Psychic
  )

   val impenetrableDefensePsychic: RedAbility = RedAbility(
    "Impenetrable Defense",
    AbilityCategory.Action,
    List(Action.Defend),
    List(
      "Defend using",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Psychic)),
      "with your Max+Mid dice against all Attacks against you until your next turn."
    ),
    PowerCategory.Psychic
  )

   val impossibleKnowledge: RedAbility = RedAbility(
    "Impossible Knowledge",
    AbilityCategory.Inherent,
    List(),
    List(
      "At the start of your turn, change any penalty into a bonus.",
    ),
    PowerCategory.Psychic
  )

   val summonedAlliesPsychic: RedAbility = RedAbility(
    "Summoned Allies",
    AbilityCategory.Action,
    List(),
    List(
      "Use",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Psychic)),
      s"to create a number of ${Die.d(6)} minions equal to your Mid die. Choose the one same basic action that they each perform. They all act at the start of your turn."
    ),
    PowerCategory.Psychic
  )

  val psychicPowerAbilities: List[RedAbility] = List(
      dangerousHinder,
      direControl,
      finalWrathPsychic,
      giveTimePsychic,
      impenetrableDefensePsychic,
      impossibleKnowledge,
      summonedAlliesPsychic,
  )

  val changeSelf: RedAbility = RedAbility(
    "Change Self",
    AbilityCategory.Inherent,
    List(),
    List(
      "At the start of your turn, swap two of your power dice. They stay swapped until changed again or the scene ends.",
    ),
    PowerCategory.SelfControl
  )

  val empowerment: RedAbility = RedAbility(
    "Empowerment",
    AbilityCategory.Reaction,
    List(Action.Defend, Action.Boost),
    List(
      "When you are Attacked, roll your single",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.SelfControl)),
      "die as a Defend against that Attack. Also Boost yourself with that same roll."
    ),
    PowerCategory.SelfControl
  )

  val impenentrableDefenseSelfControl: RedAbility = RedAbility(
    "Impenetrable Defense",
    AbilityCategory.Action,
    List(Action.Defend),
    List(
      "Defend using",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.SelfControl)),
      "with your Max+Mid dice against all Attacks against you until your next turn."
    ),
    PowerCategory.SelfControl
  )

  val majorRegenerationSelfControl: RedAbility = RedAbility(
    "Major Regeneration",
    AbilityCategory.Action,
    List(Action.Hinder, Action.Recover),
    List(
      "Hinder yourself using",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.SelfControl)),
      ". Use your Min die. Recover Health equal to your Max+Mid dice."
    ),
    PowerCategory.SelfControl
  )

  val defensiveDeflection: RedAbility = RedAbility(
    "Defensive Deflection",
    AbilityCategory.Reaction,
    List(Action.Attack, Action.Defend),
    List(
      "When you would be dealt damage, you may roll your single",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.SelfControl)),
      "die as a Defend against that damage and as an Attack against a nearby target other than the source of that damage."
    ),
    PowerCategory.SelfControl
  )

  val mutableForm: RedAbility = RedAbility(
    "Mutable Form",
    AbilityCategory.Action,
    List(Action.Boost, Action.Hinder, Action.Defend, Action.Attack, Action.Overcome),
    List(
      "Choose three basic actions. Use",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.SelfControl)),
      "in your pool and take one action with your Max die, a different action with your Mid die, and a third action with your Min die."
    ),
    PowerCategory.SelfControl
  )

  val powerfulStrikeSelfControl: RedAbility = RedAbility(
    "Powerful Strike",
    AbilityCategory.Action,
    List(),
    List(
      "Attack using",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.SelfControl)),
      ". Use your Max+Mid dice."
    ),
    PowerCategory.SelfControl
  )

  val resurrection: RedAbility = RedAbility(
    "Resurrection",
    AbilityCategory.Inherent,
    List(),
    List(
      "Once per issue,, if you would go to 0 Health, roll",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.SelfControl)),
      "+",
      QualityChoice(AbilityChoice.qualityCategories(List(QualityCategory.Physical, QualityCategory.Mental))),
      "Red zone die. Your Health becomes that number."
    ),
    PowerCategory.SelfControl
  )

  val summonedAlliesSelfControl: RedAbility = RedAbility(
    "Summoned Allies",
    AbilityCategory.Action,
    List(),
    List(
      "Use",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.SelfControl)),
      s"to create a number of ${Die.d(6)} minions equal to your Mid die. Choose the one same basic action that they each perform. They all act at the start of your turn."
    ),
    PowerCategory.SelfControl
  )

  val selfControlPowerAbilities: List[RedAbility] = List(
      changeSelf,
      empowerment,
      impenentrableDefenseSelfControl,
      majorRegenerationSelfControl,
      defensiveDeflection,
      mutableForm,
      powerfulStrikeSelfControl,
      resurrection,
      summonedAlliesSelfControl,
  )

  val combustion: RedAbility = RedAbility(
    "Combustion",
    AbilityCategory.Action,
    List(Action.Attack),
    List(
      "Attack multiple nearby targets using",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Technological)),
      ". Use your Max+Mid dice. Take irreducible damage equal to your Min die."
    ),
    PowerCategory.Technological
  )

  val finalWrathTechnological: RedAbility = RedAbility(
    "Final Wrath",
    AbilityCategory.Action,
    List(Action.Attack),
    List(
      "Attack using",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Technological)),
      ". Use your Max+Mid+Min dice. Take a major twist."
    ),
    PowerCategory.Technological
  )

  val fullDefensive: RedAbility = RedAbility(
    "Full Defensive",
    AbilityCategory.Action,
    List(Action.Hinder),
    List(
      "Hinder yourself by rolling your single",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Technological)),
      "die. You are immune to damage until the start of your next turn. You cannot use this ability again this scene."
    ),
    PowerCategory.Technological
  )

  val ultimateWeaponryTechnological: RedAbility = RedAbility(
    "Ultimate Weaponry",
    AbilityCategory.Action,
    List(Action.Boost, Action.Attack),
    List(
      "Boost yourself using",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Technological)),
      ". Use your Max die. That bonus is persistent and exclusive. Then, Attack using your Mid die plus that bonus."
    ),
    PowerCategory.Technological
  )

  val unload: RedAbility = RedAbility(
    "Unload",
    AbilityCategory.Action,
    List(Action.Attack),
    List(
      "Attack multiple targets using",
      PowerChoice(AbilityChoice.powerCategory(PowerCategory.Technological)),
      ", using your Max+Min dice. If you roll doubles, take a minor twist or damage equal to your Mid die."
    ),
    PowerCategory.Technological
  )

  val technologicalPowerAbilities: List[RedAbility] = List(
      combustion,
      finalWrathTechnological,
      fullDefensive,
      ultimateWeaponryTechnological,
      unload,
  )

  val criticalEye: RedAbility = RedAbility(
    "Critical Eye",
    AbilityCategory.Action,
    List(Action.Boost),
    List(
      "Select a target. Boost using",
      QualityChoice(AbilityChoice.qualityCategory(QualityCategory.Information)),
      ". Use your Max+Mid+Min dice. That bonus must be used against that target before the end of your next turn, or it is wasted."
    ),
    QualityCategory.Information
  )

  val discernWeakness: RedAbility = RedAbility(
    "Discern Weakness",
    AbilityCategory.Action,
    List(Action.Hinder),
    List(
      "Remove a bonus on a target. Hinder that target using",
      QualityChoice(AbilityChoice.qualityCategory(QualityCategory.Information)),
      ". Use your Max die, and that penalty is persistent and exclusive."
    ),
    QualityCategory.Information
  )

  val reliableAptitudeInformation: RedAbility = RedAbility(
    "Reliable Aptitude",
    AbilityCategory.Inherent,
    List(),
    List(
      "When taking any action using",
      QualityChoice(AbilityChoice.qualityCategory(QualityCategory.Information)),
      ", you may reroll your Min die before determining effects."
    ),
    QualityCategory.Information
  )

  val specializedInfo: RedAbility = RedAbility(
    "Specialized Info",
    AbilityCategory.Action,
    List(Action.Overcome),
    List(
      "Overcome using",
      QualityChoice(AbilityChoice.qualityCategory(QualityCategory.Information)),
      ". Use your Max+Min dice."
    ),
    QualityCategory.Information
  )

  val informationQualityAbilities: List[RedAbility] = List(
      criticalEye,
      discernWeakness,
      reliableAptitudeInformation,
      specializedInfo,
  )

  val awareResponse: RedAbility = RedAbility(
    "Aware Response",
    AbilityCategory.Reaction,
    List(Action.Attack),
    List(
      "After an opponent Attacks or Hinders you or a nearby ally, Attack the opponent by rolling your single",
      QualityChoice(AbilityChoice.qualityCategory(QualityCategory.Mental)),
      "die"
    ),
    QualityCategory.Mental
  )

  val cannyAwareness: RedAbility = RedAbility(
    "Canny Awareness",
    AbilityCategory.Action,
    List(Action.Overcome, Action.Hinder),
    List(
      "Overcome using",
      QualityChoice(AbilityChoice.qualityCategory(QualityCategory.Mental)),
      ". Use your Max+Min dice. Hinder all nearby opponents with your Mid die."
    ),
    QualityCategory.Mental
  )

  val consideredPlanning: RedAbility = RedAbility(
    "Considered Planning",
    AbilityCategory.Action,
    List(Action.Boost, Action.Defend, Action.Overcome),
    List(
      "Boost using",
      QualityChoice(AbilityChoice.qualityCategory(QualityCategory.Mental)),
      "and use your Max die. Defend against all Attacks against you using your Mid die until your next turn. Note your Min die result: as a Reaction, until your next turn, you may Hinder an attacker using that result."
    ),
    QualityCategory.Mental
  )

  val finalWrathMental: RedAbility = RedAbility(
    "Final Wrath",
    AbilityCategory.Action,
    List(Action.Attack),
    List(
      "Attack using",
      QualityChoice(AbilityChoice.qualityCategory(QualityCategory.Mental)),
      ". Use your Max+Mid+Min dice. Take a major twist."
    ),
    QualityCategory.Mental
  )

  val harmony: RedAbility = RedAbility(
    "Harmony",
    AbilityCategory.Inherent,
    List(),
    List(
      "As long as you have at least one bonus created from",
      QualityChoice(AbilityChoice.qualityCategory(QualityCategory.Mental)),
      ", treat",
      PowerChoice(),
      s"as one size higher (max ${Die.d(12)})"
    ),
    QualityCategory.Mental
  )

  val purificationMental: RedAbility = RedAbility(
    "Purification",
    AbilityCategory.Action,
    List(),
    List(
      "Remove all bonuses and penalties from the scene. You cannot use this ability again this scene.",
    ),
    QualityCategory.Mental
  )

  val mentalQualityAbilities: List[RedAbility] = List(
      awareResponse,
      cannyAwareness,
      consideredPlanning,
      finalWrathMental,
      harmony,
      purificationMental,
  )

  val bookIt: RedAbility = RedAbility(
    "Book It",
    AbilityCategory.Action,
    List(Action.Hinder),
    List(
      "Hinder any number of close targets using",
      QualityChoice(AbilityChoice.qualityCategory(QualityCategory.Physical)),
      ". Use your Max die. End your turn elsewhere in the scene."
    ),
    QualityCategory.Physical
  )

  val enduranceFighting: RedAbility = RedAbility(
    "Endurance Fighting",
    AbilityCategory.Inherent,
    List(Action.Attack, Action.Hinder),
    List(
      "Whenever you Attack a target with an action, you may also Hinder that target with your Min die.",
    ),
    QualityCategory.Physical
  )

  val finishingBlow: RedAbility = RedAbility(
    "Finishing Blow",
    AbilityCategory.Action,
    List(Action.Attack),
    List(
      "Attacking using",
      QualityChoice(AbilityChoice.qualityCategory(QualityCategory.Physical)),
      ". Use your Max die. Remove any number of penalties from the target. Add your Min die to the Attack each time you remove a penalty."
    ),
    QualityCategory.Physical
  )

  val reactiveDefense: RedAbility = RedAbility(
    "Reactive Defense",
    AbilityCategory.Reaction,
    List(Action.Defend),
    List(
      "When an opponent Attacks, you may become the target of that Attack and Defend by rolling your sngle",
      QualityChoice(AbilityChoice.qualityCategory(QualityCategory.Physical)),
      "die."
    ),
    QualityCategory.Physical
  )

  val physicalQualityAbilities: List[RedAbility] = List(
      bookIt,
      enduranceFighting,
      finishingBlow,
      reactiveDefense,
  )

  val heroicSacrifice: RedAbility = RedAbility(
    "Heroic Sacrifice",
    AbilityCategory.Reaction,
    List(Action.Defend),
    List(
      "When an opponent Attacks, you may become the target of that Attack and Defend by rolling your single Red zone die.",
    ),
    QualityCategory.Social
  )

  val inspiringTotem: RedAbility = RedAbility(
    "Inspiring Totem",
    AbilityCategory.Inherent,
    List(),
    List(
      "When you use an ability action, you may also perform any one basic action using your Mid die on the same roll.",
    ),
    QualityCategory.Social
  )

  val leadByExample: RedAbility = RedAbility(
    "Lead by Example",
    AbilityCategory.Action,
    List(Action.Boost),
    List(
      "Make a basic action using",
      QualityChoice(AbilityChoice.qualityCategory(QualityCategory.Social)),
      ". Use your Max die. All other heroes who take the same basic action on their turn against the same target receive a Boost from your Mid+Min dice."
    ),
    QualityCategory.Social
  )

  val ultimatum: RedAbility = RedAbility(
    "Ultimatum",
    AbilityCategory.Action,
    List(Action.Hinder, Action.Boost),
    List(
      "Hinder using",
      QualityChoice(AbilityChoice.qualityCategory(QualityCategory.Social)),
      ". Use your Max+Min dice. Boost yourself or an ally with your Mid die."
    ),
    QualityCategory.Social
  )

  val socialQualityAbilities: List[RedAbility] = List(
      heroicSacrifice,
      inspiringTotem,
      leadByExample,
      ultimatum,
  )

  val allRedAbilities: List[RedAbility] =
    athleticPowerAbilities ++
      energyPowerAbilities ++
      hallmarkPowerAbilities ++
      intellectualPowerAbilities ++
      materialsPowerAbilities ++
      mobilityPowerAbilities ++
      psychicPowerAbilities ++
      selfControlPowerAbilities ++
      technologicalPowerAbilities ++
      informationQualityAbilities ++
      mentalQualityAbilities ++
      physicalQualityAbilities ++
      socialQualityAbilities

  val redAbilityLookup: Map[AbilityId, RedAbility] =
    allRedAbilities.map(a => a.abilityTemplate.id -> a).toMap

  val baseRedAbilityPool: AbilityPool = AbilityPool(
    2,
    allRedAbilities.map(_.abilityTemplate)
  )
end RedAbility
