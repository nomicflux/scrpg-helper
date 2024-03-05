package scrpgHelper.chargen

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die

final class CharacterModel:
    val qualities: Var[List[(Quality, Die)]] = Var(List())
    val qualitiesSignal = qualities.signal

    val abilities: Var[List[Ability[_ <: Ability[_]]]] = Var(List())
    val abilitiesSignal = abilities.signal

    val addQuality: Observer[(Quality, Die)] = qualities.updater { case (l, (q, d)) =>
      l :+ (q, d)
    }
    val removeQuality: Observer[(Quality, Die)] = qualities.updater { case (l, (q, d)) =>
      l.filter(qd => qd._1 != q)
    }

    val addAbility: Observer[Ability[_ <: Ability[_]]] = abilities.updater { (l, a) =>
      l :+ a
    }
    val removeAbility: Observer[Ability[_ <: Ability[_]]] = abilities.updater { (l, a) =>
      l.filter(_ != a)
    }

end CharacterModel
