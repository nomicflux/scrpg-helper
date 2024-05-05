package scrpgHelper.chargen

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die
import scrpgHelper.status.Status

object RenderCharacter:
  def renderCharacter(
      character: CharacterModel,
      closeCharSheet: Observer[Unit]
  ): Element =
    div(
      className := "character-sheet",
      h2("Character"),
      div(
        child <-- character.backgroundSignal.map(renderBackground),
        child <-- character.powerSourceSignal.map(renderPowerSource),
        child <-- character.archetypeSignal.map(renderArchetype),
        child <-- character.personalitySignal.map(renderPersonality)
      ),
      div(
        className := "qualities-and-powers",
        child <-- character.allPowers.map(renderPowers),
        child <-- character.allQualities.map(renderQualities),
        child <-- character.personalitySignal.map(renderStatus),
      ),
      div(
        child <-- character.allAbilities.map(as =>
          renderAbilities(as.sortBy(_.status))
        )
      ),
      closeSheet(closeCharSheet)
    )

  def missingDescription: String = "<none>"

  def descriptionClass[A](m: Option[A]): String =
    if m.isEmpty then "char-description-missing" else "char-description-present"

  def renderBackground(background: Option[Background]): Element =
    div(
      className := "background-description char-description",
      className := descriptionClass(background),
      "Background: ",
      span(className := "char-description-field", background.fold(missingDescription)(_.name))
    )

  def renderPowerSource(powerSource: Option[PowerSource]): Element =
    div(
      className := "power-source-description char-description",
      className := descriptionClass(powerSource),
      "Power Source: ",
      span(className := "char-description-field", powerSource.fold(missingDescription)(_.name))
    )

  def renderArchetype(archetype: Option[Archetype]): Element =
    div(
      className := "archetype-description char-description",
      className := descriptionClass(archetype),
      "Archetype: ",
      span(className := "char-description-field", archetype.fold(missingDescription)(_.name))
    )

  def renderPersonality(personality: Option[Personality]): Element =
    div(
      className := "personality-description char-description",
      className := descriptionClass(personality),
      "Personality: ",
      span(className := "char-description-field", personality.fold(missingDescription)(_.name))
    )

  def renderAbilities(qualities: List[Ability[_]]): Element =
    div(
      className := "abilities",
      h3("abilities"),
      ul(
        className := "ability-list",
        qualities.map(a => li(renderAbility(a)))
      )
    )

  def renderAbility(ability: Ability[_]): Element =
    span(
      className := "chosen-ability",
      ability match
        case ca: ChosenAbility => RenderAbility.renderChosenAbility(ca)
        case p: Principle => RenderAbility.renderPrinciple(p)
        case _: Ability[_] => span("Don't know what to do")
    )

  def renderQualities(qualities: List[(Quality, Die)]): Element =
    div(
      className := "qualities pqs",
      h3("Qualities"),
      table(
        className := "quality-list",
        qualities.map(qd => renderQuality(qd._1, qd._2))
      )
    )

  def renderQuality(quality: Quality, die: Die): Element =
    tr(
      className := "quality pq",
      td(className := "pq-name", quality.name),
      td(className := "pq-die-size", die.toString)
    )

  def renderPowers(powers: List[(Power, Die)]): Element =
    div(
      className := "powers pqs",
      h3("Powers"),
      table(
        className := "power-list",
        powers.map(qd => renderPower(qd._1, qd._2))
      )
    )

  def renderPower(power: Power, die: Die): Element =
    tr(
      className := "power pq",
      td(className := "pq-name", power.name),
      td(className := "pq-die-size", die.toString)
    )

  def renderStatus(mp: Option[Personality]): Element =
    div(
      className := "status",
      h3("Status"),
      table(
        className := "status-list",
        tr(
          className := "green-status status-row",
          td(className := "status-name", "Green"),
          td(
            className := "green-status-die status-die",
            mp.flatMap(_.statusDice.get(Status.Green).map(_.toString)).getOrElse("")
          )
        ),
        tr(
          className := "yellow-status status-row",
          td(className := "status-name", "Yellow"),
          td(
            className := "yellow-status-die status-die",
            mp.flatMap(_.statusDice.get(Status.Yellow).map(_.toString)).getOrElse("")
          )
        ),
        tr(
          className := "red-status status-row",
          td(className := "status-name", "Red"),
          td(
            className := "red-status-die status-die",
            mp.flatMap(_.statusDice.get(Status.Red).map(_.toString)).getOrElse("")
          )
        ),
      )
    )

  def closeSheet(closeCharSheet: Observer[Unit]): Element =
    div(
      className := "close-char-sheet",
      button(
        tpe := "button",
        "X",
        onClick --> { _ev => closeCharSheet.onNext(()) }
      )
    )
end RenderCharacter