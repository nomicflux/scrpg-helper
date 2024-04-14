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
      renderRollButton(model.rollTrigger, character),
      renderShownToggle(model.showUnchosenSignal, model.shownToggle),
      renderPowerSourceTable(character)
    )
  end renderPowerSources

  def renderRollButton(
      rollTrigger: Observer[List[Die]],
      character: CharacterModel
  ): Element =
    div(
      button(
        tpe := "button",
        "Roll",
        onClick
          .compose(
            _.withCurrentValueOf(
              character.backgroundSignal.map(_.toList.flatMap(_.powerSourceDice))
            )
          ) --> { (_, dice) =>
          rollTrigger.onNext(dice)
        }
      )
    )
  end renderRollButton

  def renderShownToggle(
      shown: Signal[Boolean],
      shownToggle: Observer[Unit]
  ): Element =
    div(
      button(
        tpe := "button",
        child.text <-- shown.map(b => if b then "Hide" else "Show"),
        " Power Sources",
        onClick --> { _ => shownToggle.onNext(()) }
      )
    )
  end renderShownToggle

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
      className <-- character.powerSourceSignal.map(mps =>
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
          character.backgroundSignal.map(mbg =>
            mbg.fold(List())(_.powerSourceDice)
          ),
          powerSource.powerList,
          powerSource.extraPower,
          character
            .powersSignal(Signal.fromValue(Some(powerSource)))
            .combineWith(
              character.backgroundSignal.map(mbg =>
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
            .combineWith(character.qualitiesSignal(character.backgroundSignal))
            .map { (l, bqs) =>
              val qualitySet: Set[Quality] =
                l.map(_._1).toSet.union(bqs.map(_._1).toSet)
              (qd, _) => qualitySet.contains(qd._1)
            },
          character.removeQuality(powerSource),
          character.addQuality(powerSource)
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

  def renderPowers(
      dicePool: Signal[List[Die]],
      powers: List[Power],
      extraPower: Option[(Die, List[Power])],
      powerAllowed: Signal[((Power, Die), Option[(Power, Die)]) => Boolean],
      removePower: Observer[(Power, Die)],
      addPower: Observer[(Power, Die)]
  ): Element =
    div(
      children <-- dicePool
        .map(ds =>
          ds.map { d =>
            span(
              className := "choice-die-box",
              d.toString,
              ":",
              SelectWithPrevChoice[(Power, Die)](
                powers.map(p => (p, d)),
                pd => pd._1.name
              )
                .render(powerAllowed, removePower, addPower)
            )
          }
        ),
      extraPower.toList.map { case (d, ps) =>
        span(
          className := "choice-die-box",
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
            className := "choice-die-box",
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
