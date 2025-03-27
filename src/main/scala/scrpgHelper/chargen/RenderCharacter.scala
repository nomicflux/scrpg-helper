package scrpgHelper.chargen

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die
import scrpgHelper.status.Status

object RenderCharacter:
  val model = new RenderCharacterModel()

  def renderCharacter(
      character: CharacterModel,
      closeCharSheet: Observer[Unit]
  ): Element =
    div(
      className := "character-sheet",
      h2("Character Sheet"),
      div(
        className := "export-section",
        className <-- model.showExport.signal.map(b => if b then "" else "hidden"),
        pre(
          child.text <-- character.forExport.map(_.render)
        )
      ),
      div(
        className := "description-section",
        className <-- model.showExport.signal.map(b => if b then "hidden" else ""),
        div(
          className := "details-panel",
          div(
            table(
              child <-- character.background.signal.map(renderBackground),
              child <-- character.powerSource.signal.map(renderPowerSource),
              child <-- character.archetype.signal.map(renderArchetype),
              child <-- character.personality.signal.map(renderPersonality)
            )
          )
        ),
        div(
          className := "health-panel",
          child <-- character.health.signal.map(renderHealth)
        )
      ),
      div(
        className := "qualities-and-powers",
        className <-- model.showExport.signal.map(b => if b then "hidden" else ""),
        child <-- character.allPowers.map(renderPowers),
        child <-- character.allQualities.map(renderQualities),
        child <-- character.personality.signal.map(renderStatus)
      ),
      div(
        className <-- model.showExport.signal.map(b => if b then "hidden" else ""),
        child <-- character.allAbilities.map(as =>
          renderAbilities(as.sortBy(_.status))
        )
      ),
      exportSheet(),
      closeSheet(closeCharSheet)
    )

  def missingDescription: String = "<none>"

  def descriptionClass[A](m: Option[A]): String =
    if m.isEmpty then "char-description-missing" else "char-description-present"

  def renderHealth(health: Option[Int]): Element =
    val healthMarks: Option[(Int, Int, Int)] = health.map(Health.calcRanges(_))
    div(
      h3("Health"),
      table(
        tr(
          className := "green-health health-row",
          td(className := "health-name", "Green"),
          td(
            className := "green-health-die health-die",
            healthMarks.fold("")(hm => s"${hm._1} - ${hm._2 + 1}")
          )
        ),
        tr(
          className := "yellow-health health-row",
          td(className := "health-name", "Yellow"),
          td(
            className := "yellow-health-die health-die",
            healthMarks.fold("")(hm => s"${hm._2} - ${hm._3 + 1}")
          )
        ),
        tr(
          className := "red-health health-row",
          td(className := "health-name", "Red"),
          td(
            className := "red-health-die health-die",
            healthMarks.fold("")(hm => s"${hm._3} - 1")
          )
        )
      )
    )
  end renderHealth

  def renderBackground(background: Option[Background]): Element =
    tr(
      className := "background-description char-description",
      className := descriptionClass(background),
      td("Background:"),
      td(
        span(
          className := "char-description-field",
          background.fold(missingDescription)(_.name)
        )
      )
    )

  def renderPowerSource(powerSource: Option[PowerSource]): Element =
    tr(
      className := "power-source-description char-description",
      className := descriptionClass(powerSource),
      td("Power Source:"),
      td(
        span(
          className := "char-description-field",
          powerSource.fold(missingDescription)(_.name)
        )
      )
    )

  def renderArchetype(archetype: Option[Archetype]): Element =
    tr(
      className := "archetype-description char-description",
      className := descriptionClass(archetype),
      td("Archetype:"),
      td(
        span(
          className := "char-description-field",
          archetype.fold(missingDescription)(_.name)
        )
      )
    )

  def renderPersonality(personality: Option[Personality]): Element =
    tr(
      className := "personality-description char-description",
      className := descriptionClass(personality),
      td("Personality:"),
      td(
        span(
          className := "char-description-field",
          personality.fold(missingDescription)(_.name)
        )
      )
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
        case p: Principle      => RenderAbility.renderPrinciple(p)
        case _: Ability[_]     => span("Don't know what to do")
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
            mp.flatMap(_.statusDice.get(Status.Green).map(_.toString))
              .getOrElse("")
          )
        ),
        tr(
          className := "yellow-status status-row",
          td(className := "status-name", "Yellow"),
          td(
            className := "yellow-status-die status-die",
            mp.flatMap(_.statusDice.get(Status.Yellow).map(_.toString))
              .getOrElse("")
          )
        ),
        tr(
          className := "red-status status-row",
          td(className := "status-name", "Red"),
          td(
            className := "red-status-die status-die",
            mp.flatMap(_.statusDice.get(Status.Red).map(_.toString))
              .getOrElse("")
          )
        )
      )
    )

  def exportSheet(): Element =
    div(
      className := "export-char-sheet",
      button(
        tpe := "button",
        child.text <-- model.showExport.signal.map(b => if b then "Character Sheet" else "To Copyable Text"),
        onClick --> { _ => model.toggleExport.onNext(()) }
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

final class RenderCharacterModel:
    val showExport: Var[Boolean] = Var(false)
    val toggleExport: Observer[Unit] = showExport.updater { (b, _) => !b }
end RenderCharacterModel
