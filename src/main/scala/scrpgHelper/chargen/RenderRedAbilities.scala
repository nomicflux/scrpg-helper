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
      className := "red-abilities-categorized",
      renderCategorizedAbilityPool(character, qualities, powers)
    )

  private def renderCategorizedAbilityPool(
      character: CharacterModel,
      qualities: List[Quality],
      powers: List[Power]
  ): Element =
    // Keep the exact same pool logic as original, just organize the display
    val categoryGroups = List(
      ("Athletic Power Abilities", RedAbility.athleticPowerAbilities),
      ("Energy Power Abilities", RedAbility.energyPowerAbilities),
      ("Hallmark Power Abilities", RedAbility.hallmarkPowerAbilities),
      ("Intellectual Power Abilities", RedAbility.intellectualPowerAbilities),
      ("Materials Power Abilities", RedAbility.materialsPowerAbilities),
      ("Mobility Power Abilities", RedAbility.mobilityPowerAbilities),
      ("Psychic Power Abilities", RedAbility.psychicPowerAbilities),
      ("Self Control Power Abilities", RedAbility.selfControlPowerAbilities),
      ("Technological Power Abilities", RedAbility.technologicalPowerAbilities),
      ("Information Quality Abilities", RedAbility.informationQualityAbilities),
      ("Mental Quality Abilities", RedAbility.mentalQualityAbilities),
      ("Physical Quality Abilities", RedAbility.physicalQualityAbilities),
      ("Social Quality Abilities", RedAbility.socialQualityAbilities)
    )

    // Create a custom renderAbilityPool that groups abilities by category but keeps same pool logic
    div(
      className := "ability-pool",
      span(s"Pick ${RedAbility.baseRedAbilityPool.max}:"),
      div(
        categoryGroups.map { case (categoryName, categoryAbilities) =>
          renderAbilityCategory(categoryName, categoryAbilities, character, qualities, powers)
        }
      )
    )

  private def renderAbilityCategory(
      categoryName: String,
      categoryAbilities: List[RedAbility],
      character: CharacterModel,
      qualities: List[Quality],
      powers: List[Power]
  ): Element =
    // Filter to abilities that are both in this category AND allowed for the character
    val allowedCategoryAbilities = categoryAbilities.filter(_.allowed(qualities, powers))

    if allowedCategoryAbilities.nonEmpty then
      div(
        className := "red-ability-category",
        h3(categoryName, className := "category-header"),
        div(
          className := "category-abilities",
          allowedCategoryAbilities.map(redAbility =>
            RenderAbility.renderAbility(
              character,
              RedAbility.redAbilityPhase,
              RedAbility.baseRedAbilityPool,
              redAbility.abilityTemplate,
              character.abilityChoicesSignal(RedAbility.redAbilityPhase)
            )
          )
        )
      )
    else
      div()
end RenderRedAbilities
