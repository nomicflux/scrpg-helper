package scrpgHelper.chargen

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die

final class CharacterModel:
    val qualities: Var[List[(Quality, Die)]] = Var(List())
    val abilities: Var[List[Ability[_ <: Ability[_]]]] = Var(List())
    val background: Var[Option[Background]] = Var(None)
    val backgroundSignal = background.signal
    val changeBackground: Observer[Background] = background.updater { (_, b) => Some(b) }

    type StagingKey = Background

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

    val abilityStaging: Var[Map[StagingKey, List[Ability[_ <: Ability[_]]]]] = Var(Map())
    def abilitiesSignal(stagingKey: Signal[Option[StagingKey]]): Signal[List[Ability[_ <: Ability[_]]]] =
      abilities.signal.combineWith(abilityStaging.signal, stagingKey).map((as, m, mb) => as ++ mb.flatMap(b => m.get(b)).getOrElse(List()))

    def addAbility(stagingKey: StagingKey): Observer[Ability[_ <: Ability[_]]] = abilityStaging.updater { (m, a) =>
      val newList = m.getOrElse(stagingKey, List()) :+ a
      m + (stagingKey -> newList)
    }
    def removeAbility(stagingKey: StagingKey): Observer[Ability[_ <: Ability[_]]] = abilityStaging.updater { (m, a) =>
      val newList = m.getOrElse(stagingKey, List()).filter(_ != a)
      m + (stagingKey -> newList)
    }

    val validBackground: Signal[Boolean] = backgroundSignal.combineWith(qualityStaging.signal, abilityStaging.signal).map { (mb, qm, am) =>
      mb.fold(false)(b => b.valid(qm.getOrElse(b, List()),
                                  am.getOrElse(b, List())))
    }
end CharacterModel
