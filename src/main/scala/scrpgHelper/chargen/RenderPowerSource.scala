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
        renderAbilityPools(
          character,
          powerSource.abilityPools
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

  def renderAbilityPools(character: CharacterModel, abilityPools: List[AbilityPool]): Element =
    div(
      abilityPools.map(renderAbilityPool(character, _))
    )
  end renderAbilityPools

  def renderAbilityPool(character: CharacterModel, abilityPool: AbilityPool): Element =
    div(
      className := "ability-pool",
      span(s"Pick ${abilityPool.max}:"),
      renderAbilities(character, abilityPool.abilities)
    )
  end renderAbilityPool

  def renderAbilities(character: CharacterModel, abilities: List[AbilityTemplate]): Element =
    div(
      abilities.map(renderAbility(character, _))
    )
  end renderAbilities

  def renderAbility(character: CharacterModel, ability: AbilityTemplate): Element =
    div(
      className := s"ability status-${ability.status.toString.toLowerCase()}",
      span(
        child.text <-- character.abilityChoiceSignal(ability).map(acs => ability.actions(acs).map(_.toSymbol).foldLeft("")(_ + _))
      ),
      span(ability.name),
      span(ability.category.toAbbreviation),
      span(
        className := "ability-description",
        child.text <-- character.abilityChoiceSignal(ability).map(acs => ability.description(acs))
      ),
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
