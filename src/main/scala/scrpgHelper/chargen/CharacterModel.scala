package scrpgHelper.chargen

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die

final class CharacterModel:
  val qualities: Var[List[(Quality, Die)]] = Var(List())
  val powers: Var[List[(Power, Die)]] = Var(List())
  val abilities: Var[List[Ability[_]]] = Var(List())
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

  type StagingKey = Background | PowerSource | Archetype

  val qualityStaging: Var[Map[StagingKey, List[(Quality, Die)]]] = Var(Map())
  def qualitiesSignal(
      stagingKey: Signal[Option[StagingKey]]
  ): Signal[List[(Quality, Die)]] =
    qualities.signal
      .combineWith(qualityStaging.signal, stagingKey)
      .map((qs, m, mb) => qs ++ mb.flatMap(b => m.get(b)).getOrElse(List()))

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
    powers.signal
      .combineWith(powerStaging.signal, stagingKey)
      .map((ps, m, mps) => ps ++ mps.flatMap(ps => m.get(ps)).getOrElse(List()))

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
    abilities.signal
      .combineWith(abilityStaging.signal, stagingKey)
      .map((as, m, mb) => as ++ mb.flatMap(b => m.get(b)).getOrElse(List()))

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

  val abilityChoice: Var[Map[StagingKey, Map[AbilityTemplate, ChosenAbility]]] =
    Var(
      (PowerSource.powerSources
        .map(ps =>
          ps ->
            (ps.abilityPools
              .flatMap(ap => ap.abilities.map(a => a -> a.toChosenAbility(ap)))
              .toMap)
        ) ++
        Archetype.archetypes
          .map(at =>
            at ->
              (at.abilityPools
                .flatMap(ap =>
                  ap.abilities.map(a => a -> a.toChosenAbility(ap))
                )
                .toMap)
          )).toMap
    )
  def abilityChoicesSignal(
      stagingKey: StagingKey
  ): Signal[Map[AbilityTemplate, ChosenAbility]] =
    abilityChoice.signal.map(acs => acs.get(stagingKey).head)
  def addAbilityChoice(
      stagingKey: StagingKey,
      ability: AbilityTemplate
  ): Observer[AbilityChoice] =
    abilityChoice.updater { (acs, choice) =>
      val choices: Map[AbilityTemplate, ChosenAbility] =
        acs.get(stagingKey).head
      val currChoice: ChosenAbility = choices.get(ability).head
      acs + (stagingKey -> (choices + (ability -> currChoice.applyChoice(
        choice
      ))))
    }
  def removeAbilityChoice(
      stagingKey: StagingKey,
      ability: AbilityTemplate
  ): Observer[AbilityChoice] =
    abilityChoice.updater { (acs, choice) =>
      val choices: Map[AbilityTemplate, ChosenAbility] =
        acs.get(stagingKey).head
      val currChoice: ChosenAbility = choices.get(ability).head
      acs + (stagingKey -> (choices + (ability -> currChoice.removeChoice(
        choice
      ))))
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
        val abilityMap: Map[AbilityTemplate, ChosenAbility] =
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
      ).map { (mat, dice, pm, qm, asm, am) =>
        mat.fold(false) { at =>
          val powers: List[Power] = pm.getOrElse(at, List()).map(_._1)
          val qualities: List[Quality] = qm.getOrElse(at, List()).map(_._1)
          val selectedAbilities: Set[AbilityId] = asm
            .getOrElse(at, List())
            .collect { case ca: ChosenAbility => ca }
            .map(_.id)
            .toSet
          val abilityMap: Map[AbilityTemplate, ChosenAbility] =
            am.getOrElse(at, Map())
          val abilities: List[ChosenAbility] =
            abilityMap.values.toList.filter(a => selectedAbilities.contains(a.id))
          at.valid(dice, powers, qualities, abilities)
        }
      }
end CharacterModel
