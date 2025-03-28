package scrpgHelper.chargen

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die

object RenderPowerSource:
  import scrpgHelper.components.SelectWithPrevChoice

  val model = new PowerSourceModel()

  def renderPowerSources(character: CharacterModel): Element =
    div(
      className := "power-source-section choice-section",
      h2("Power Source"),
      RollComponent.renderRollButton(
        model.rollTrigger,
        character.background.signal.map(_.toList.flatMap(_.powerSourceDice))
      ),
      RollComponent.renderShownToggle(
        model.rollsSignal,
        model.showUnchosenSignal,
        model.shownToggle,
        "Power Sources"
      ),
      renderPowerSourceTable(character)
    )
  end renderPowerSources

  def renderPowerSourceTable(character: CharacterModel): Element =
    table(
      tr(
        th(),
        th("Power Source"),
        th("Powers"),
        th("Abilities"),
        th("Archetype Dice")
      ),
      PowerSource.powerSources.map {
        (
            powerSource =>
              renderPowerSourceRow(
                character,
                powerSource
              )
        )
      }
    )
  end renderPowerSourceTable

  def mandatoryPowerCheck(dicePool: List[Die], powerSource: PowerSource)(
      pds: List[(Power, Die)]
  )(pd: (Power, Die), prevChoice: Option[(Power, Die)]): Boolean =
    val baseSet = pds.map(_._1).toSet
    val newSet = prevChoice.fold(baseSet)(pc => baseSet - pc._1) + pd._1
    powerSource.mandatoryPowers.toSet
      .diff(newSet)
      .isEmpty || newSet.size < dicePool.size
  end mandatoryPowerCheck

  def renderPowerSourceRow(
      character: CharacterModel,
      powerSource: PowerSource
  ): Element =
    tr(
      className := "power-source-row",
      className <-- model.rollsSignal
        .combineWith(model.showUnchosenSignal)
        .map { (mrolls, shown) =>
          mrolls.fold("undecided") { rolls =>
            if (rolls.contains(powerSource.number)) {
              "chosen"
            } else {
              if shown then "unchosen" else "hidden"
            }
          }
        },
      className <-- character.powerSource.signal.map(mps =>
        if mps.fold(false)(_ == powerSource) then "picked" else "unpicked"
      ),
      td(
        powerSource.number.toString
      ),
      td(
        h3(
          powerSource.name
        )
      ),
      td(
        renderPowers(
          character.background.signal.map(mbg =>
            mbg.fold(List())(_.powerSourceDice)
          ),
          powerSource.powerList,
          powerSource.extraPower,
          character
            .powersSignal(Signal.fromValue(Some(powerSource)))
            .combineWith(
              character.background.signal.map(mbg =>
                mbg.fold(List())(_.powerSourceDice)
              )
            )
            .map { (l, dicePool) =>
              val powerSet: Set[Power] = l.map(_._1).toSet
              (pd, mpc) =>
                powerSet.contains(pd._1) || !mandatoryPowerCheck(
                  dicePool,
                  powerSource
                )(l)(pd, mpc)
            },
          character.removePower(powerSource),
          character.addPower(powerSource)
        ),
        renderQuality(
          powerSource.extraQuality,
          character
            .qualitiesSignal(Signal.fromValue(Some(powerSource)))
            .combineWith(character.qualitiesSignal(character.background.signal))
            .map { (l, bqs) =>
              val qualitySet: Set[Quality] =
                l.map(_._1).toSet.union(bqs.map(_._1).toSet)
              (qd, _) => qualitySet.contains(qd._1)
            },
          character.removeQuality(powerSource),
          character.addQuality(powerSource)
        ),
        powerSource.upgrades.fold(span())(upgrades =>
          renderDieChange(
            "Upgrade",
            character
              .qualitiesSignal(Signal.fromValue(Some(powerSource)))
              .combineWith(
                character.qualitiesSignal(character.background.signal)
              )
              .map { (psqs, bgqs) => psqs ++ bgqs },
            character.powersSignal(Signal.fromValue(Some(powerSource))),
            powerSource.upgrades,
            character.upgrade(powerSource),
            character.downgrade(powerSource)
          )
        ),
        powerSource.downgrades.fold(span())(upgrades =>
          renderDieChange(
            "Downgrade",
            character
              .qualitiesSignal(Signal.fromValue(Some(powerSource)))
              .combineWith(
                character.qualitiesSignal(character.background.signal)
              )
              .map { (psqs, bgqs) => psqs ++ bgqs },
            character.powersSignal(Signal.fromValue(Some(powerSource))),
            powerSource.upgrades,
            character.downgrade(powerSource),
            character.upgrade(powerSource)
          )
        )
      ),
      td(
        div(
          powerSource.abilityPools.map(abilityPool =>
            RenderAbility.renderAbilityPool(character, powerSource, abilityPool)
          )
        )
      ),
      td(
        powerSource.archetypeDiePool
          .map(_.toString)
          .reduceLeft(_ + " , " + _)
      ),
      onMouseDown --> { _ =>
        character.changePowerSource.onNext(powerSource)
      },
      onFocus --> { _ =>
        character.changePowerSource.onNext(powerSource)
      },
      onClick --> { _ =>
        character.changePowerSource.onNext(powerSource)
      }
    )
  end renderPowerSourceRow

  def renderDieChange(
      text: String,
      qualities: Signal[List[(Quality, Die)]],
      powers: Signal[List[(Power, Die)]],
      changeable: Option[((Quality | Power, Die) => Boolean)],
      forwardChange: Observer[Quality | Power],
      reverseChange: Observer[Quality | Power]
  ): Element =
    val pqs: Signal[List[Quality | Power]] =
      powers.combineWith(qualities).map { (ps, qs) =>
        changeable.fold(List()) { fn =>
          (ps ++ qs).filter(pqd => fn(pqd._1, pqd._2)).map(_._1)
        }
      }
    div(
      span(
        className := s"choice-die-box ${text.toLowerCase()}-list",
        className <-- pqs.map(l => if l.isEmpty then "hidden" else ""),
        text,
        ": ",
        SelectWithPrevChoice[Quality | Power](
          pqs,
          qp =>
            qp match
              case p: Power   => p.name
              case q: Quality => q.name
        ).render(Signal.fromValue((_, _) => false), reverseChange, forwardChange)
      )
    )

  def renderPowers(
      dicePool: Signal[List[Die]],
      powers: List[Power],
      extraPower: List[Die] => Option[(Die, List[Power])],
      powerAllowed: Signal[((Power, Die), Option[(Power, Die)]) => Boolean],
      removePower: Observer[(Power, Die)],
      addPower: Observer[(Power, Die)]
  ): Element =
    div(
      children <-- dicePool
        .map(ds =>
          ds.map { d =>
            span(
              className := "choice-die-box power-list",
              d.render,
              ":",
              SelectWithPrevChoice[(Power, Die)](
                powers.map(p => (p, d)),
                pd => pd._1.name
              )
                .render(powerAllowed, removePower, addPower)
            )
          }
        ),
      children <-- dicePool.map(dp =>
        extraPower(dp).toList.map { case (d, ps) =>
          span(
            className := "choice-die-box power-list",
            d.toString,
            ":",
            SelectWithPrevChoice[(Power, Die)](
              ps.map(p => (p, d)),
              pd => pd._1.name
            )
              .render(powerAllowed, removePower, addPower)
          )
        }
      )
    )
  end renderPowers

  def renderQuality(
      extraQuality: Option[(Die, List[Quality])],
      qualityAllowed: Signal[
        ((Quality, Die), Option[(Quality, Die)]) => Boolean
      ],
      removeQuality: Observer[(Quality, Die)],
      addQuality: Observer[(Quality, Die)]
  ): Element =
    div(
      extraQuality.toList
        .map { case (d, q) =>
          span(
            className := "choice-die-box quality-list",
            d.toString,
            ":",
            SelectWithPrevChoice[(Quality, Die)](
              extraQuality.toList.flatMap(_._2).map(q => (q, d)),
              qd => qd._1.name
            )
              .render(qualityAllowed, removeQuality, addQuality)
          )
        }
    )
  end renderQuality

end RenderPowerSource

final class PowerSourceModel:
  import scrpgHelper.rolls.Die.d

  val rolls: Var[Option[Set[Int]]] = Var(None)
  val rollsSignal = rolls.signal
  val showUnchosen: Var[Boolean] = Var(false)
  val showUnchosenSignal = showUnchosen.signal

  val rollTrigger: Observer[List[Die]] = rolls.updater { (_, ds) =>
    Some(Die.rollForCharGen(ds))
  }

  val shownToggle: Observer[Unit] = showUnchosen.updater { (b, _) => !b }

end PowerSourceModel
