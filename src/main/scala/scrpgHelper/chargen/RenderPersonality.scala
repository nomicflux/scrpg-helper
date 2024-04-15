package scrpgHelper.chargen
import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die

object RenderPersonality:
  val model = new PersonalityModel()

  def renderPersonalities(character: CharacterModel): Element =
    div(
      className := "personality-section choice-section",
      h2("Personality"),
      renderRollButton(model.rollTrigger),
      renderShownToggle(model.showUnchosenSignal, model.shownToggle)
    )

  def renderRollButton(rollTrigger: Observer[Unit]): Element =
    div(
      button(
        tpe := "button",
        "Roll",
        onClick --> { _ => rollTrigger.onNext(()) }
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
        " Backgrounds",
        onClick --> { _ => shownToggle.onNext(()) }
      )
    )
  end renderShownToggle
end RenderPersonality

final class PersonalityModel:
  import scrpgHelper.rolls.Die
  import scrpgHelper.rolls.Die.d

  val rolls: Var[Option[Set[Int]]] = Var(None)
  val rollsSignal = rolls.signal
  val showUnchosen: Var[Boolean] = Var(false)
  val showUnchosenSignal = showUnchosen.signal

  val rollTrigger: Observer[Unit] = rolls.updater { (_, _) =>
    Some(Die.rollForCharGen(List(d(10), d(10))))
  }

  val shownToggle: Observer[Unit] = showUnchosen.updater { (b, _) => !b }
end PersonalityModel
