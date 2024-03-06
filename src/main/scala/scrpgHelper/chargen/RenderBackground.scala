package scrpgHelper.chargen

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die

object RenderBackground:
  import scrpgHelper.components.SelectWithPrevChoice

  def renderBackgrounds(character: CharacterModel): Element =
    val model = new BackgroundModel()

    div(
      className := "background-section choice-section",
      h2("Backgrounds"),
      renderRollButton(model.rollTrigger),
      renderShownToggle(model.showUnchosenSignal, model.shownToggle),
      renderBackgroundTable(model.rollsSignal, model.showUnchosenSignal, character)
   )
  end renderBackgrounds

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

  def renderBackgroundTable(
    rollsSignal: Signal[Option[Set[Int]]],
    shownSignal: Signal[Boolean],
    character: CharacterModel,
  ): Element =
    val pickedBackground: Var[Option[Background]] = Var(None)
    val pickedBackgroundSignal = pickedBackground.signal
    val changeBackground: Observer[Background] = pickedBackground.updater { (mb, b) =>
      Some(b)
    }

    table(
      tr(
        th(),
        th("Background"),
        th(colSpan := 2,
           "Principle of"),
        th("Qualities"),
        th("Power Source Dice"),
      ),
      Background.backgrounds.map(
        renderBackgroundRow(
          rollsSignal,
          shownSignal,
          pickedBackgroundSignal,
          changeBackground,
          character,
          _
        )
      )
    )
  end renderBackgroundTable

  def renderBackgroundRow(
      rollsSignal: Signal[Option[Set[Int]]],
      shownSignal: Signal[Boolean],
      pickedBackgroundSignal: Signal[Option[Background]],
      changeBackground: Observer[Background],
      character: CharacterModel,
      background: Background
  ): Element =
    tr(
      className := "background-row",
      className <-- rollsSignal.combineWith(shownSignal).map {
        (mrolls, shown) =>
          mrolls.fold("undecided") { rolls =>
            if (rolls.contains(background.number)) {
              "chosen"
            } else {
              if shown then "unchosen" else "hidden"
            }
          }
      },
      className <-- pickedBackgroundSignal.map(mb => if mb.fold(false)(_ == background) then "picked" else "unpicked"),
      td(background.number.toString),
      td(h3(background.name)),
      td(s"(${background.principleCategory.toString})"),
      td(
        renderPrinciples(
          Principle.categoryToPrinciples(background.principleCategory),
          character.abilitiesSignal(pickedBackgroundSignal).map(l => l.map(_.name).toSet),
          character.removeAbility(background),
          character.addAbility(background),
        )
      ),
      td(
        renderQualities(
          background.backgroundDice,
          background.mandatoryQualities ++ background.qualityList,
          character.qualitiesSignal(pickedBackgroundSignal).map(l => l.map(_._1.name).toSet),
          character.removeQuality(background),
          character.addQuality(background),
        )
      ),
      td(background.powerSourceDice.map(_.toString).reduceLeft(_ + " , " + _)),
      onMouseDown --> { _ => changeBackground.onNext(background) },
      onFocus --> { _ => changeBackground.onNext(background) },
      onClick --> { _ => changeBackground.onNext(background) },
    )
  end renderBackgroundRow

  def renderQualities(
    dice: List[Die],
    qualities: List[Quality],
    charQualities: Signal[Set[String]],
    removeQuality: Observer[(Quality, Die)],
    addQuality: Observer[(Quality, Die)],
  ): Element =
    div(
      dice.map { d =>
        span(
          className := "choice-die-box",
          d.toString,
          ":",
          SelectWithPrevChoice(qualities.map(q => (q, d)), qd => qd._1.name)
            .render(charQualities,
                    removeQuality,
                    addQuality)
        )
      }
    )
  end renderQualities

  def renderPrinciples(principles: List[Principle],
                       charAbilities: Signal[Set[String]],
                       removePrinciple: Observer[Principle],
                       addPrinciple: Observer[Principle],
  ): Element =
    SelectWithPrevChoice(principles, p => p.name)
      .render(charAbilities,
              removePrinciple,
              addPrinciple)
  end renderPrinciples
end RenderBackground

final class BackgroundModel:
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

end BackgroundModel
