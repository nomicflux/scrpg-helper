package scrpgHelper.chargen

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die

object RenderRedAbilities:
  def renderRedAbilities(character: CharacterModel): Element =
    div(
      className := "red-ability-section choice-section",
      h2("Red Abilities"),
      div(
        child <-- character.allQualities.combineWith(character.allPowers).map {
          (qs, ps) =>
            renderAbilitySelect(character, qs.map(_._1), ps.map(_._1))
        }
      )
    )

  def renderAbilitySelect(
      character: CharacterModel,
      qualities: List[Quality],
      powers: List[Power]
  ): Element =
    div(
      RenderAbility.renderAbilityPool(
        character,
        RedAbility.redAbilityPhase,
        RedAbility.baseRedAbilityPool,
        ability =>
          RedAbility.redAbilityLookup
          .get(ability.id)
          .fold(false)(ra => ra.allowed(qualities, powers))
      )
    )
end RenderRedAbilities
