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
    val changeBackground: Observer[Background] = background.updater { (_, b) => Some(b) }
    val powerSource: Var[Option[PowerSource]] = Var(None)
    val powerSourceSignal = powerSource.signal
    val chosenPowerSourceSignal = backgroundSignal.combineWith(powerSourceSignal).map { (mbg, mps) =>
      mbg.flatMap(bg => mps.map(ps => PowerSourceWithChoices(ps, bg.powerSourceDice, List())))
    }
    val changePowerSource: Observer[PowerSource] = powerSource.updater { (_, ps) => Some(ps) }

    type StagingKey = Background | PowerSource

    val qualityStaging: Var[Map[StagingKey, List[(Quality, Die)]]] = Var(Map())
    def qualitiesSignal(stagingKey: Signal[Option[StagingKey]]): Signal[List[(Quality, Die)]] =
      qualities.signal.combineWith(qualityStaging.signal, stagingKey).map((qs, m, mb) => qs ++ mb.flatMap(b => m.get(b)).getOrElse(List()))

    def addQuality(stagingKey: StagingKey): Observer[(Quality, Die)] = qualityStaging.updater { case (m, (q, d)) =>
      val newList = m.getOrElse(stagingKey, List()) :+ (q, d)
      m + (stagingKey -> newList)
    }
    def removeQuality(stagingKey: StagingKey): Observer[(Quality, Die)] = qualityStaging.updater { case (m, (q, d)) =>
      val newList = m.getOrElse(stagingKey, List()).filter(_._1 != q)
      m + (stagingKey -> newList)
    }

    val powerStaging: Var[Map[StagingKey, List[(Power, Die)]]] = Var(Map())
    def powersSignal(stagingKey: Signal[Option[StagingKey]]): Signal[List[(Power, Die)]] =
      powers.signal.combineWith(powerStaging.signal, stagingKey).map((ps, m, mps) => ps ++ mps.flatMap(ps => m.get(ps)).getOrElse(List()))

    def addPower(stagingKey: StagingKey): Observer[(Power, Die)] = powerStaging.updater { case (m, (p, d)) =>
      val newList = m.getOrElse(stagingKey, List()) :+ (p, d)
      m + (stagingKey -> newList)
    }
    def removePower(stagingKey: StagingKey): Observer[(Power, Die)] = powerStaging.updater { case (m, (p, d)) =>
      val newList = m.getOrElse(stagingKey, List()).filter(_._1 != p)
      m + (stagingKey -> newList)
    }

    val abilityStaging: Var[Map[StagingKey, List[Ability[_]]]] = Var(Map())
    def abilitiesSignal(stagingKey: Signal[Option[StagingKey]]): Signal[List[Ability[_]]] =
      abilities.signal.combineWith(abilityStaging.signal, stagingKey).map((as, m, mb) => as ++ mb.flatMap(b => m.get(b)).getOrElse(List()))

    def addAbility(stagingKey: StagingKey): Observer[Ability[_]] = abilityStaging.updater { (m, a) =>
      val newList = m.getOrElse(stagingKey, List()) :+ a
      m + (stagingKey -> newList)
    }
    def removeAbility(stagingKey: StagingKey): Observer[Ability[_]] = abilityStaging.updater { (m, a) =>
      val newList = m.getOrElse(stagingKey, List()).filter(_ != a)
      m + (stagingKey -> newList)
    }

    val abilityChoices: Var[Map[AbilityTemplate, List[AbilityChoice]]] = Var(Map())
    def abilityChoiceSignal(ability: AbilityTemplate): Signal[List[AbilityChoice]] =
      abilityChoices.signal.map(m => m.getOrElse(ability, ability.baseChoices))

    val validBackground: Signal[Boolean] = backgroundSignal.combineWith(qualityStaging.signal, abilityStaging.signal).map { (mb, qm, am) =>
      mb.fold(false)(b => b.valid(qm.getOrElse(b, List()),
                                  am.getOrElse(b, List())))
    }
end CharacterModel
