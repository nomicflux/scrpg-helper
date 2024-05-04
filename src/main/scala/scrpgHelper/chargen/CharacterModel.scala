package scrpgHelper.chargen

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die

final class CharacterModel:
  val background: Var[Option[Background]] = Var(None)
  val backgroundSignal = background.signal
  val changeBackground: Observer[Background] = background.updater { (_, b) =>
    Some(b)
  }

  val powerSource: Var[Option[PowerSource]] = Var(None)
  val powerSourceSignal = powerSource.signal
  val changePowerSource: Observer[PowerSource] =
    powerSource.updater { (_, ps) => Some(ps) }

  val archetype: Var[Option[Archetype]] = Var(None)
  val archetypeSignal = archetype.signal
  val changeArchetype: Observer[Archetype] =
    archetype.updater { (_, at) => Some(at) }

  val personality: Var[Option[Personality]] = Var(None)
  val personalitySignal = personality.signal
  val changePersonality: Observer[Personality] =
    personality.updater { (_, p) => Some(p) }

  val health: Var[Option[Int]] = Var(None)
  val healthSignal = health.signal
  val changeHealth: Observer[Int] =
    health.updater { (_, n) => Some(n) }

  type StagingKey = Background | PowerSource | Archetype | Personality |
    RedAbility.RedAbilityPhase

  val qualityStaging: Var[Map[StagingKey, List[(Quality, Die)]]] = Var(Map())
  def qualitiesSignal(
      stagingKey: Signal[Option[StagingKey]]
  ): Signal[List[(Quality, Die)]] =
    qualityStaging.signal
      .combineWith(stagingKey)
      .map((m, mb) => mb.flatMap(b => m.get(b)).getOrElse(List()))

  def addQuality(stagingKey: StagingKey): Observer[(Quality, Die)] =
    qualityStaging.updater { case (m, (q, d)) =>
      val newList = m.getOrElse(stagingKey, List()) :+ (q, d)
      m + (stagingKey -> newList)
    }
  def removeQuality(stagingKey: StagingKey): Observer[(Quality, Die)] =
    qualityStaging.updater { case (m, (q, d)) =>
      val newList = m.getOrElse(stagingKey, List()).filter(_._1 != q)
      m + (stagingKey -> newList)
    }

  val powerStaging: Var[Map[StagingKey, List[(Power, Die)]]] = Var(Map())
  def powersSignal(
      stagingKey: Signal[Option[StagingKey]]
  ): Signal[List[(Power, Die)]] =
    powerStaging.signal
      .combineWith(stagingKey)
      .map((m, mps) => mps.flatMap(ps => m.get(ps)).getOrElse(List()))

  def addPower(stagingKey: StagingKey): Observer[(Power, Die)] =
    powerStaging.updater { case (m, (p, d)) =>
      val newList = m.getOrElse(stagingKey, List()) :+ (p, d)
      m + (stagingKey -> newList)
    }
  def removePower(stagingKey: StagingKey): Observer[(Power, Die)] =
    powerStaging.updater { case (m, (p, d)) =>
      val newList = m.getOrElse(stagingKey, List()).filter(_._1 != p)
      m + (stagingKey -> newList)
    }

  val abilityStaging: Var[Map[StagingKey, List[Ability[_]]]] = Var(Map())
  def abilitiesSignal(
      stagingKey: Signal[Option[StagingKey]]
  ): Signal[List[Ability[_]]] =
    abilityStaging.signal
      .combineWith(stagingKey)
      .map((m, mb) => mb.flatMap(b => m.get(b)).getOrElse(List()))

  def addAbility(stagingKey: StagingKey): Observer[Ability[_]] =
    abilityStaging.updater { (m, a) =>
      val newList = m.getOrElse(stagingKey, List()) :+ a
      m + (stagingKey -> newList)
    }
  def removeAbility(stagingKey: StagingKey): Observer[Ability[_]] =
    abilityStaging.updater { (m, a) =>
      val newList = m.getOrElse(stagingKey, List()).filter(_ != a)
      m + (stagingKey -> newList)
    }

  def toggleAbility(stagingKey: StagingKey): Observer[ChosenAbility] =
    abilityStaging.updater { (m, a) =>
      val currList = m.getOrElse(stagingKey, List())
      val newList =
        if currList.contains(a) then currList.filter(_ != a)
        else (currList :+ a)
      if a.inPool.runValidation(newList.collect { case ca: ChosenAbility =>
          ca
        })
      then m + (stagingKey -> newList)
      else m
    }

  val powerSourceAbilities: List[(PowerSource, Map[AbilityId, ChosenAbility])] =
    PowerSource.powerSources
      .map(ps =>
        ps ->
          (ps.abilityPools
            .flatMap(ap => ap.abilities.map(a => a.id -> a.toChosenAbility(ap)))
            .toMap)
      )

  val archetypeAbilities: List[(Archetype, Map[AbilityId, ChosenAbility])] =
    Archetype.archetypes
      .map(at =>
        at ->
          (at.abilityPools
            .flatMap(ap => ap.abilities.map(a => a.id -> a.toChosenAbility(ap)))
            .toMap)
      )

  val personalityAbilities: List[(Personality, Map[AbilityId, ChosenAbility])] =
    Personality.personalities
      .map(pt =>
        pt -> (pt.outAbilityPool.abilities
          .map(a => a.id -> a.toChosenAbility(pt.outAbilityPool))
          .toMap)
      )

  val redAbilities: List[
    (RedAbility.RedAbilityPhase, Map[AbilityId, ChosenAbility])
  ] =
    List(
      RedAbility.redAbilityPhase -> RedAbility.baseRedAbilityPool.abilities
        .map(a => a.id -> a.toChosenAbility(RedAbility.baseRedAbilityPool))
        .toMap
    )

  val baseAbilities: Map[StagingKey, Map[AbilityId, ChosenAbility]] =
    (powerSourceAbilities ++ archetypeAbilities ++ personalityAbilities ++ redAbilities).toMap

  val abilityChoice: Var[Map[StagingKey, Map[AbilityId, ChosenAbility]]] =
    Var(baseAbilities)
  def abilityChoicesSignal(
      stagingKey: StagingKey
  ): Signal[Map[AbilityId, ChosenAbility]] =
    abilityChoice.signal.map(acs => acs.getOrElse(stagingKey, Map()))
  def addAbilityChoice(
      stagingKey: StagingKey,
      ability: AbilityTemplate
  ): Observer[AbilityChoice] =
    abilityChoice.updater { (acs, choice) =>
      val choices: Map[AbilityId, ChosenAbility] =
        acs.getOrElse(stagingKey, Map())
      val mCurrChoice: Option[ChosenAbility] =
        choices.get(ability.id).headOption
      mCurrChoice.fold(acs) { currChoice =>
        acs + (stagingKey -> (choices + (ability.id -> currChoice.applyChoice(
          choice
        ))))
      }
    }
  def removeAbilityChoice(
      stagingKey: StagingKey,
      ability: AbilityTemplate
  ): Observer[AbilityChoice] =
    abilityChoice.updater { (acs, choice) =>
      val choices: Map[AbilityId, ChosenAbility] =
        acs.getOrElse(stagingKey, Map())
      val mCurrChoice: Option[ChosenAbility] = choices.get(ability.id)
      mCurrChoice.fold(acs) { currChoice =>
        acs + (stagingKey -> (choices + (ability.id -> currChoice.removeChoice(
          choice
        ))))
      }
    }

  val allQualities: Signal[List[(Quality, Die)]] = qualityStaging.signal
    .combineWith(
      backgroundSignal,
      powerSourceSignal,
      archetypeSignal,
      personalitySignal
    )
    .map((quals, mbg, mps, mat, mpt) =>
      mbg.fold(List())(bg => quals.getOrElse(bg, List())) ++
        mps.fold(List())(ps => quals.getOrElse(ps, List())) ++
        mat.fold(List())(at => quals.getOrElse(at, List())) ++
        mpt.fold(List())(pt => quals.getOrElse(pt, List()))
    )

  val allPowers: Signal[List[(Power, Die)]] = powerStaging.signal
    .combineWith(
      backgroundSignal,
      powerSourceSignal,
      archetypeSignal,
      personalitySignal
    )
    .map((pows, mbg, mps, mat, mpt) =>
      mbg.fold(List())(bg => pows.getOrElse(bg, List())) ++
        mps.fold(List())(ps => pows.getOrElse(ps, List())) ++
        mat.fold(List())(at => pows.getOrElse(at, List())) ++
        mpt.fold(List())(pt => pows.getOrElse(pt, List()))
    )

  val allStagedAbilities: Signal[List[ChosenAbility]] = abilityStaging.signal
    .combineWith(
      backgroundSignal,
      powerSourceSignal,
      archetypeSignal,
      personalitySignal
    )
    .map((abils, mbg, mps, mat, mpt) =>
      mbg.fold(List())(bg => abils.getOrElse(bg, List())) ++
        mps.fold(List())(ps => abils.getOrElse(ps, List())) ++
        mat.fold(List())(at => abils.getOrElse(at, List())) ++
        mpt.fold(List())(pt => abils.getOrElse(pt, List())) ++
        abils.getOrElse(RedAbility.redAbilityPhase, List())
    )
    .map(_.collect { case ca: ChosenAbility => ca })

  val allChosenAbilities: Signal[List[ChosenAbility]] = abilityChoice.signal
    .combineWith(
      backgroundSignal,
      powerSourceSignal,
      archetypeSignal,
      personalitySignal
    )
    .map((abils, mbg, mps, mat, mpt) =>
      mbg.fold(List())(bg => abils.getOrElse(bg, List())) ++
        mps.fold(List())(ps => abils.getOrElse(ps, List())) ++
        mat.fold(List())(at => abils.getOrElse(at, List())) ++
        mpt.fold(List())(pt => abils.getOrElse(pt, List())) ++
        abils.getOrElse(RedAbility.redAbilityPhase, List())
    )
    .map(_.map(_._2).filter(_.descriptionFilledOut).toList)

  val allAbilities: Signal[List[ChosenAbility]] =
    allStagedAbilities
      .combineWith(allChosenAbilities)
      .map { (asa, aca) =>
        val abilityIds = asa.map(_.id).toSet
        aca.filter(a => abilityIds.contains(a.id))
      }

  val validBackground: Signal[Boolean] = backgroundSignal
    .combineWith(qualityStaging.signal, abilityStaging.signal)
    .map { (mb, qm, am) =>
      mb.fold(false)(b =>
        b.valid(qm.getOrElse(b, List()), am.getOrElse(b, List()))
      )
    }

  val validPowerSource: Signal[Boolean] = powerSourceSignal
    .combineWith(
      backgroundSignal.map((mb: Option[Background]) =>
        mb.toList.flatMap((b: Background) => b.powerSourceDice)
      ),
      powerStaging.signal,
      qualityStaging.signal,
      abilityStaging.signal,
      abilityChoice.signal
    )
    .map { (mp, dice, pm, qm, asm, am) =>
      mp.fold(false) { p =>
        val powers: List[Power] = pm.getOrElse(p, List()).map(_._1)
        val qualities: List[Quality] = qm.getOrElse(p, List()).map(_._1)
        val selectedAbilities: Set[AbilityId] = asm
          .getOrElse(p, List())
          .collect { case ca: ChosenAbility => ca }
          .map(_.id)
          .toSet
        val abilityMap: Map[AbilityId, ChosenAbility] =
          am.getOrElse(p, Map())
        val abilities: List[ChosenAbility] =
          abilityMap.values.toList.filter(a => selectedAbilities.contains(a.id))
        p.valid(dice, powers, qualities, abilities)
      }
    }

  val validArchetype: Signal[Boolean] = archetypeSignal
    .combineWith(
      powerSourceSignal.map(_.toList.flatMap(_.archetypeDiePool)),
      powerStaging.signal,
      qualityStaging.signal,
      abilityStaging.signal,
      abilityChoice.signal
    )
    .map { (mat, dice, pm, qm, asm, am) =>
      mat.fold(false) { at =>
        val powers: List[Power] = pm.getOrElse(at, List()).map(_._1)
        val qualities: List[Quality] = qm.getOrElse(at, List()).map(_._1)
        val selectedAbilities: Set[AbilityId] = asm
          .getOrElse(at, List())
          .collect { case ca: ChosenAbility => ca }
          .map(_.id)
          .toSet
        val abilityMap: Map[AbilityId, ChosenAbility] =
          am.getOrElse(at, Map())
        val abilities: List[ChosenAbility] =
          abilityMap.values.toList.filter(a => selectedAbilities.contains(a.id))
        at.valid(dice, powers, qualities, abilities)
      }
    }

  val validPersonality: Signal[Boolean] = personalitySignal
    .combineWith(
      powerStaging.signal,
      qualityStaging.signal,
      abilityStaging.signal,
      abilityChoice.signal
    )
    .map { (mp, pm, qm, asm, am) =>
      mp.fold(false) { p =>
        val qualities: List[Quality] = qm.getOrElse(p, List()).map(_._1)
        val selectedAbilities: Set[AbilityId] = asm
          .getOrElse(p, List())
          .collect { case ca: ChosenAbility => ca }
          .map(_.id)
          .toSet
        val abilityMap: Map[AbilityId, ChosenAbility] =
          am.getOrElse(p, Map())
        val abilities: List[ChosenAbility] =
          abilityMap.values.toList.filter(a => selectedAbilities.contains(a.id))
        p.valid(qualities, abilities)
      }
    }

  val validRedAbilities: Signal[Boolean] =
    abilityStaging.signal.combineWith(abilityChoice.signal).map { (as, am) =>
      val selectedAbilities: Set[AbilityId] = as
        .getOrElse(RedAbility.redAbilityPhase, List())
        .collect { case ca: ChosenAbility => ca }.map(_.id).toSet
      val abilityMap: Map[AbilityId, ChosenAbility] =
        am.getOrElse(RedAbility.redAbilityPhase, Map())
      val redAbilities: List[ChosenAbility] =
        abilityMap.values.toList.filter(a => selectedAbilities.contains(a.id))
      redAbilities.size == RedAbility.baseRedAbilityPool.max &&
      redAbilities.map(_.valid).foldLeft(true)(_ && _)
    }

  val validHealth: Signal[Boolean] = healthSignal.map(_.isDefined)
end CharacterModel
