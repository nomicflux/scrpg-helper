package scrpgHelper.chargen

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die

object RenderPowerSource:
  import scrpgHelper.components.SelectWithPrevChoice

  def renderPowerSources(character: CharacterModel): Element =
    div(
      className := "power-source-section choice-section",
      h2("Power Source"),
      renderPowerSourceTable(character),
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
        PowerSource.powerSources.map(ps => renderPowerSourceRow(character, ps))
      )
  end renderPowerSourceTable

  def renderPowerSourceRow(character: CharacterModel, powerSource: PowerSource): Element =
    tr(
      className := "power-source-row",
      className <-- character.powerSourceSignal.map(mps => if mps.fold(false)(_ == powerSource) then "picked" else "unpicked"),
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
          character.backgroundSignal.map(mbg => mbg.fold(List())(_.powerSourceDice)),
          powerSource.powerList,
          character.powersSignal(character.powerSourceSignal).map{l =>
            val powerSet: Set[Power] = l.map(_._1).toSet
            (pd, _) => powerSet.contains(pd._1)
          },
          character.removePower(powerSource),
          character.addPower(powerSource),
        )
      ),
      td(
        div(
          powerSource.abilityPools.map(renderAbilityPool(character, powerSource, _))
        )
      ),
      td(
        powerSource.archetypeDiePool.map(_.toString).reduceLeft(_ + " , " + _)
      ),
      onMouseDown --> { _ => character.changePowerSource.onNext(powerSource) },
      onFocus --> { _ => character.changePowerSource.onNext(powerSource) },
      onClick --> { _ => character.changePowerSource.onNext(powerSource) },
    )
  end renderPowerSourceRow

  def renderAbilityPool(character: CharacterModel,
                        powerSource: PowerSource,
                        abilityPool: AbilityPool): Element =
    div(
      className := "ability-pool",
      span(s"Pick ${abilityPool.max}:"),
      div(
        abilityPool.abilities.map(renderAbility(character, powerSource, abilityPool, _))
      )
    )
  end renderAbilityPool

  def renderAbility(character: CharacterModel,
                    powerSource: PowerSource,
                    abilityPool: AbilityPool,
                    template: AbilityTemplate): Element =
    val chosenAbility: Var[ChosenAbility] = Var(template.toChosenAbility(abilityPool))
    val chosenSignal = chosenAbility.signal

    div(
      className := s"ability status-${template.status.toString.toLowerCase()}",
      className <-- character.abilitiesSignal(Signal.fromValue(Some(powerSource))).map(l => if l.collect{ case ca: ChosenAbility => ca.template}.contains(template) then "ability-selected" else "ability-unselected"),
      span(
        className := "ability-actions",
        child.text <-- chosenSignal.map(_.actions.map(_.toSymbol).foldLeft("")(_ + _))
      ),
      span(
        className := "ability-name",
        child.text <-- chosenSignal.map(_.name)
      ),
      span(
        className := "ability-category",
        child.text <-- chosenSignal.map(_.category.toAbbreviation)
      ),
      span(
        className := "ability-description",
        child.text <-- chosenSignal.map(_.description)
      ),
      onClick.compose(_.withCurrentValueOf(chosenSignal)) --> { (_, chosen) => character.toggleAbility(powerSource).onNext(chosen) },
    )
  end renderAbility

  def renderPowers(dicePool: Signal[List[Die]],
                   powers: List[Power],
                   powerAllowed: Signal[((Power, Die), Option[(Power, Die)]) => Boolean],
                   removePower: Observer[(Power, Die)],
                   addPower: Observer[(Power, Die)]
  ): Element =
    div(
      children <-- dicePool
        .map(ds => ds.map {d =>
                                  span(
                                    className := "choice-die-box",
                                    d.toString,
                                    ":",
                                    SelectWithPrevChoice(powers.map(p => (p, d)), pd => pd._1.name)
                                      .render(powerAllowed,
                                              removePower,
                                              addPower)
                                  )
                                })
    )
  end renderPowers
end RenderPowerSource
