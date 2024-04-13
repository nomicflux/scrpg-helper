package scrpgHelper.chargen
import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die

object RenderArchetype:
  import scrpgHelper.components.SelectWithPrevChoice

  val model = new ArchetypeModel()

  def renderArchetypes(character: CharacterModel): Element =
    div(
      className := "archetype-section choice-section",
      h2("Archetype"),
      renderRollButton(model.rollTrigger, character),
      renderShownToggle(model.showUnchosenSignal, model.shownToggle),
      renderArchetypeTable(character)
    )

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
              character.powerSourceSignal.map(
                _.toList.flatMap(_.archetypeDiePool)
              )
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
        " Archetypes",
        onClick --> { _ => shownToggle.onNext(()) }
      )
    )

  def renderArchetypeTable(character: CharacterModel): Element =
    table(
    )
end RenderArchetype

final class ArchetypeModel:
  import scrpgHelper.rolls.Die.d

  val rolls: Var[Option[Set[Int]]] = Var(None)
  val rollsSignal = rolls.signal
  val showUnchosen: Var[Boolean] = Var(false)
  val showUnchosenSignal = showUnchosen.signal

  val rollTrigger: Observer[List[Die]] = rolls.updater { (_, ds) =>
    Some(Die.rollForCharGen(ds))
  }

  val shownToggle: Observer[Unit] = showUnchosen.updater { (b, _) => !b }
end ArchetypeModel
