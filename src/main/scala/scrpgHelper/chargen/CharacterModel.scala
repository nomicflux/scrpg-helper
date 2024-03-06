package scrpgHelper.chargen

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die

final class CharacterModel:
    val qualities: Var[List[(Quality, Die)]] = Var(List())

    val abilities: Var[List[Ability[_ <: Ability[_]]]] = Var(List())

    val qualityStaging: Var[Map[Background, List[(Quality, Die)]]] = Var(Map())
    def qualitiesSignal(background: Signal[Option[Background]]): Signal[List[(Quality, Die)]] =
      qualities.signal.combineWith(qualityStaging.signal, background).map((qs, m, mb) => qs ++ mb.flatMap(b => m.get(b)).getOrElse(List()))

    def addQuality(background: Background): Observer[(Quality, Die)] = qualityStaging.updater { case (m, (q, d)) =>
      val newList = m.getOrElse(background, List()) :+ (q, d)
      m + (background -> newList)
    }
    def removeQuality(background: Background): Observer[(Quality, Die)] = qualityStaging.updater { case (m, (q, d)) =>
      val newList = m.getOrElse(background, List()).filter(_._1 != q)
      m + (background -> newList)
    }

    val stagingAbilities: Var[Map[Background, List[Ability[_ <: Ability[_]]]]] = Var(Map())
    def abilitiesSignal(background: Signal[Option[Background]]): Signal[List[Ability[_ <: Ability[_]]]] =
      abilities.signal.combineWith(stagingAbilities.signal, background).map((as, m, mb) => as ++ mb.flatMap(b => m.get(b)).getOrElse(List()))

    def addAbility(background: Background): Observer[Ability[_ <: Ability[_]]] = stagingAbilities.updater { (m, a) =>
      val newList = m.getOrElse(background, List()) :+ a
      m + (background -> newList)
    }
    def removeAbility(background: Background): Observer[Ability[_ <: Ability[_]]] = stagingAbilities.updater { (m, a) =>
      val newList = m.getOrElse(background, List()).filter(_ != a)
      m + (background -> newList)
    }
end CharacterModel
