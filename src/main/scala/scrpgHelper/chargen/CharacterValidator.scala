package scrpgHelper.chargen

import scrpgHelper.chargen.characterModel.*

/** Character validator implementation - validates character data.
  *
  * Takes CharacterData and provides pure validation functions.
  * No more signals - just pure validation logic using existing validation methods.
  */
class CharacterValidator(characterData: CharacterData):

  def validBackground(data: CharacterData): Boolean =
    data.background.fold(false) { bg =>
      val qualities = data.qualityStaging.getOrElse(bg, List())
      val abilities = data.abilityStaging.getOrElse(bg, List())
      bg.valid(qualities, abilities)
    }

  def validPowerSource(data: CharacterData): Boolean =
    data.powerSource.fold(false) { ps =>
      val dice = data.background.toList.flatMap(_.powerSourceDice)
      val powers = data.powerStaging.getOrElse(ps, List())
      val qualities = data.qualityStaging.getOrElse(ps, List()).map(_._1)
      val selectedAbilities = data.abilityStaging
        .getOrElse(ps, List())
        .collect { case ca: ChosenAbility => ca }
        .map(_.key)
        .toSet
      val abilityMap = data.abilityChoice.getOrElse(ps, Map())
      val abilities = abilityMap.values.toList
        .filter(a => selectedAbilities.contains(a.key))
      ps.valid(dice, powers, qualities, abilities)
    }

  def validArchetype(data: CharacterData): Boolean =
    data.archetype.fold(false) { at =>
      val dice = data.powerSource.toList.flatMap(_.archetypeDiePool)
      val powers = data.powerStaging.getOrElse(at, List()).map(_._1)
      val allPowers = data.powerSource.fold(List())(ps =>
        data.powerStaging.getOrElse(ps, List()).map(_._1)) ++ powers
      val qualities = data.qualityStaging.getOrElse(at, List()).map(_._1)
      val allQualities = data.background.fold(List())(bg =>
        data.qualityStaging.getOrElse(bg, List()).map(_._1)) ++
        data.powerSource.fold(List())(ps =>
          data.qualityStaging.getOrElse(ps, List()).map(_._1)) ++ qualities
      val selectedAbilities = data.abilityStaging
        .getOrElse(at, List())
        .collect { case ca: ChosenAbility => ca }
        .map(_.key)
        .toSet
      val abilityMap = data.abilityChoice.getOrElse(at, Map())
      val abilities = abilityMap.values.toList
        .filter(a => selectedAbilities.contains(a.key))
      at.valid(dice, powers, qualities, abilities, allPowers, allQualities)
    }

  def validPersonality(data: CharacterData): Boolean =
    data.personality.fold(false) { pt =>
      val qualities = data.qualityStaging.getOrElse(pt, List()).map(_._1)
      val selectedAbilities = data.abilityStaging
        .getOrElse(pt, List())
        .collect { case ca: ChosenAbility => ca }
        .map(_.key)
        .toSet
      val abilityMap = data.abilityChoice.getOrElse(pt, Map())
      val abilities = abilityMap.values.toList
        .filter(a => selectedAbilities.contains(a.key))
      pt.valid(qualities, abilities)
    }

  def validRedAbilities(data: CharacterData): Boolean =
    val selectedAbilities = data.abilityStaging
      .getOrElse(RedAbility.redAbilityPhase, List())
      .collect { case ca: ChosenAbility => ca }
      .map(_.key)
      .toSet
    val abilityMap = data.abilityChoice.getOrElse(RedAbility.redAbilityPhase, Map())
    val redAbilities = abilityMap.values.toList
      .filter(a => selectedAbilities.contains(a.key))
    redAbilities.size == RedAbility.baseRedAbilityPool.max &&
    redAbilities.map(_.valid).foldLeft(true)(_ && _)

  def validHealth(data: CharacterData): Boolean =
    data.health.isDefined

end CharacterValidator