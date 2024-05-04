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
      renderShownToggle(model.showUnchosenSignal, model.shownToggle),
      renderPersonalityTable(character)
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

  def renderPersonalityTable(character: CharacterModel): Element =
    table(
      tr(
        th(),
        th("Personality"),
        th("Quality"),
        th("Out Ability")
      ),
      Personality.personalities.map(renderPersonality(character, _))
    )

  def renderPersonality(
      character: CharacterModel,
      personality: Personality
  ): Element =
    tr(
      className := "personality-row",
      className <-- model.rollsSignal
        .combineWith(model.showUnchosenSignal)
        .map { (mrolls, shown) =>
          mrolls.fold("undecided") { rolls =>
            if (rolls.contains(personality.number)) {
              "chosen"
            } else {
              if shown then "unchosen" else "hidden"
            }
          }
        },
      className <-- character.personalitySignal.map(mp =>
        if mp.fold(false)(_ == personality) then "picked" else "unpicked"
      ),
      td(personality.number.toString),
      td(personality.name),
      td(renderPersonalityQuality(character, personality)),
      td(personality.outAbilityPool.abilities.headOption.map { template =>
        RenderAbility.renderAbility(
          character,
          personality,
          personality.outAbilityPool,
          template,
          character.abilityChoicesSignal(personality)
        )
      }),
      onMouseDown --> { _ =>
        character.changePersonality.onNext(personality)
      },
      onFocus --> { _ =>
        character.changePersonality.onNext(personality)
      },
      onClick --> { _ =>
        character.changePersonality.onNext(personality)
      }
    )
  end renderPersonality

  def renderPersonalityQuality(
      character: CharacterModel,
      personality: Personality
  ): Element =
    val quality: Var[Quality] = Var(personality.baseQuality)
    val nameUpdater: Observer[String] = quality.updater { (q, n) =>
      character.removeQuality(personality).onNext((q, Die.d(8)))
      val newQ = q.changeName(n)
      character.addQuality(personality).onNext((newQ, Die.d(8)))
      newQ
    }
    div(
      Die.d(8).toString,
      ":",
      input(
        tpe := "text",
        value <-- quality.signal.map(_.name),
        onInput.mapToValue --> nameUpdater
      )
    )
  end renderPersonalityQuality
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
