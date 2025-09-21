package scrpgHelper.chargen

import com.raquo.laminar.api.L.{*, given}
import scrpgHelper.rolls.Die
import scrpgHelper.chargen.characterModel.*

/** Character validator implementation - validates character state.
  *
  * Takes CharacterState and CharacterStaging and implements CharacterValidation
  * by checking all validation rules against the current character data.
  */
class CharacterValidator(characterState: CharacterState, characterStaging: CharacterStaging) extends CharacterValidation:

  val validBackground: Signal[Boolean] = characterState.background.signal
    .combineWith(characterStaging.qualityStaging.signal, characterStaging.abilityStaging.signal)
    .map { (mb, qm, am) =>
      mb.fold(false)(b =>
        b.valid(qm.getOrElse(b, List()), am.getOrElse(b, List()))
      )
    }

  val validPowerSource: Signal[Boolean] = characterState.powerSource.signal
    .combineWith(
      characterState.background.signal.map((mb: Option[Background]) =>
        mb.toList.flatMap((b: Background) => b.powerSourceDice)
      ),
      characterStaging.powerStaging.signal,
      characterStaging.qualityStaging.signal,
      characterStaging.abilityStaging.signal,
      characterStaging.abilityChoice.signal
    )
    .map { (mp, dice, pm, qm, asm, am) =>
      mp.fold(false) { p =>
        val powers: List[(Power, Die)] = pm.getOrElse(p, List())
        val qualities: List[Quality] = qm.getOrElse(p, List()).map(_._1)
        val selectedAbilities: Set[AbilityKey] = asm
          .getOrElse(p, List())
          .collect { case ca: ChosenAbility => ca }
          .map(_.key)
          .toSet
        val abilityMap: Map[AbilityKey, ChosenAbility] =
          am.getOrElse(p, Map())
        val abilities: List[ChosenAbility] =
          abilityMap.values.toList
            .filter(a => selectedAbilities.contains(a.key))
        p.valid(dice, powers, qualities, abilities)
      }
    }

  val validArchetype: Signal[Boolean] = characterState.archetype.signal
    .combineWith(
      characterState.powerSource.signal.map(_.toList.flatMap(_.archetypeDiePool)),
      characterStaging.powerStaging.signal,
      characterStaging.qualityStaging.signal,
      characterStaging.abilityStaging.signal,
      characterStaging.abilityChoice.signal,
      characterState.background.signal,
      characterState.powerSource.signal
    )
    .map { (mat, dice, pm, qm, asm, am, mbg, mps) =>
      mat.fold(false) { at =>
        val powers: List[Power] = pm.getOrElse(at, List()).map(_._1)
        val allPowers: List[Power] =
          mps.fold(List())(ps => pm.getOrElse(ps, List()).map(_._1)) ++ powers
        val qualities: List[Quality] = qm.getOrElse(at, List()).map(_._1)
        val allQualities: List[Quality] =
          mbg.fold(List())(bg => qm.getOrElse(bg, List()).map(_._1)) ++ mps
            .fold(List())(ps => qm.getOrElse(ps, List()).map(_._1)) ++ qualities
        val selectedAbilities: Set[AbilityKey] = asm
          .getOrElse(at, List())
          .collect { case ca: ChosenAbility => ca }
          .map(_.key)
          .toSet
        val abilityMap: Map[AbilityKey, ChosenAbility] =
          am.getOrElse(at, Map())
        val abilities: List[ChosenAbility] =
          abilityMap.values.toList
            .filter(a => selectedAbilities.contains(a.key))
        at.valid(dice, powers, qualities, abilities, allPowers, allQualities)
      }
    }

  val validPersonality: Signal[Boolean] = characterState.personality.signal
    .combineWith(
      characterStaging.powerStaging.signal,
      characterStaging.qualityStaging.signal,
      characterStaging.abilityStaging.signal,
      characterStaging.abilityChoice.signal
    )
    .map { (mp, pm, qm, asm, am) =>
      mp.fold(false) { p =>
        val qualities: List[Quality] = qm.getOrElse(p, List()).map(_._1)
        val selectedAbilities: Set[AbilityKey] = asm
          .getOrElse(p, List())
          .collect { case ca: ChosenAbility => ca }
          .map(_.key)
          .toSet
        val abilityMap: Map[AbilityKey, ChosenAbility] =
          am.getOrElse(p, Map())
        val abilities: List[ChosenAbility] =
          abilityMap.values.toList
            .filter(a => selectedAbilities.contains(a.key))
        p.valid(qualities, abilities)
      }
    }

  val validRedAbilities: Signal[Boolean] =
    characterStaging.abilityStaging.signal.combineWith(characterStaging.abilityChoice.signal).map { (as, am) =>
      val selectedAbilities: Set[AbilityKey] = as
        .getOrElse(RedAbility.redAbilityPhase, List())
        .collect { case ca: ChosenAbility => ca }
        .map(_.key)
        .toSet
      val abilityMap: Map[AbilityKey, ChosenAbility] =
        am.getOrElse(RedAbility.redAbilityPhase, Map())
      val redAbilities: List[ChosenAbility] =
        abilityMap.values.toList.filter(a => selectedAbilities.contains(a.key))
      redAbilities.size == RedAbility.baseRedAbilityPool.max &&
      redAbilities.map(_.valid).foldLeft(true)(_ && _)
    }

  val validHealth: Signal[Boolean] = characterState.health.signal.map(_.isDefined)

end CharacterValidator