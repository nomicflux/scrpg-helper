package scrpgHelper.chargen

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

object CharGen:
  val model = new CharGenModel()

  def charGen(): Element =
    val character = new CharacterModel()
    div(
      h1("Character Generation"),
      div(
        toggleCharSheet(model.showCharSheetSignal, model.toggleCharSheet),
      ),
      div(
        prevPageButton(character, model.pageSignal, model.retreatPage),
        nextPageButton(character, model.pageSignal, model.advancePage)
      ),
      renderPage(model.pageSignal, model.showCharSheetSignal, model.toggleCharSheet, character)
    )
  end charGen

  def toggleCharSheet(
    showCharSheetSignal: Signal[Boolean],
    toggleCharSheet: Observer[Unit]
  ): Element =
    button(
      className := "toggle-char-sheet",
      className <-- showCharSheetSignal.map(b => if b then "char-sheet-shown" else "char-sheet-hidden"),
      tpe := "button",
      child.text <-- showCharSheetSignal.map(b => if b then "Hide" else "Show"),
      " Character Sheet",
      onClick --> { _ev => toggleCharSheet.onNext(()) },
    )

  def nextPageButton(
      character: CharacterModel,
      pageSignal: Signal[CharGenPage],
      advancePage: Observer[Unit]
  ): Element =
    button(
      tpe := "button",
      "Next Page",
      className <-- pageSignal.map(p => if (p == CharGenPage.HealthPage) then "hidden" else ""),
      disabled <-- pageSignal
        .combineWith(character.validBackground, character.validPowerSource,
                     character.validArchetype, character.validPersonality,
                     character.validRedAbilities, character.validHealth)
        .map((p, b, ps, at, pt, ra, h) =>
          p match {
            case CharGenPage.BackgroundPage  => !b
            case CharGenPage.PowerSourcePage => !ps
            case CharGenPage.ArchetypePage   => !at
            case CharGenPage.PersonalityPage => !pt
            case CharGenPage.RedAbilityPage  => !ra
            case CharGenPage.RetconPage      => false
            case CharGenPage.HealthPage      => true
          }
        ),
      onClick --> { _ => advancePage.onNext(()) }
    )
  end nextPageButton

  def prevPageButton(
      character: CharacterModel,
      pageSignal: Signal[CharGenPage],
      retreatPage: Observer[Unit]
  ): Element =
    button(
      tpe := "button",
      "Previous Page",
      className <-- pageSignal.map(p => if (p == CharGenPage.BackgroundPage) then "hidden" else ""),
      disabled <-- pageSignal.map(p =>
        p match {
          case CharGenPage.BackgroundPage => true
          case _                          => false
        }
      ),
      onClick --> { _ => retreatPage.onNext(()) }
    )
  end prevPageButton

  def renderPage(
      pageSignal: Signal[CharGenPage],
      charSheetSignal: Signal[Boolean],
      toggleCharSheet: Observer[Unit],
      character: CharacterModel
  ): Element =
    div(
      div(
        className <-- charSheetSignal.map(b => if b then "char-sheet" else "hidden"),
        RenderCharacter.renderCharacter(character, toggleCharSheet)
      ),
      div(
        className <-- pageSignal.map(p =>
          if p == CharGenPage.BackgroundPage then "" else "hidden"
        ),
        RenderBackground.renderBackgrounds(character)
      ),
      div(
        className <-- pageSignal.map(p =>
          if p == CharGenPage.PowerSourcePage then "" else "hidden"
        ),
        RenderPowerSource.renderPowerSources(character)
      ),
      div(
        className <-- pageSignal.map(p =>
          if p == CharGenPage.ArchetypePage then "" else "hidden"
        ),
        RenderArchetype.renderArchetypes(character)
      ),
      div(
        className <-- pageSignal.map(p =>
          if p == CharGenPage.PersonalityPage then "" else "hidden"
        ),
        RenderPersonality.renderPersonalities(character)
      ),
      div(
        className <-- pageSignal.map(p =>
          if p == CharGenPage.RedAbilityPage then "" else "hidden"
        ),
        RenderRedAbilities.renderRedAbilities(character)
      ),
      div(
        className <-- pageSignal.map(p =>
          if p == CharGenPage.RetconPage then "" else "hidden"
        ),
        RenderRetcon.renderRetcon(character)
      ),
      div(
        className <-- pageSignal.map(p =>
          if p == CharGenPage.HealthPage then "" else "hidden"
        ),
        RenderHealth.renderHealth(character)
      ),
    )
  end renderPage
end CharGen

enum CharGenPage:
  case BackgroundPage, PowerSourcePage, ArchetypePage, PersonalityPage, RedAbilityPage, RetconPage, HealthPage
end CharGenPage

final class CharGenModel:
  val page: Var[CharGenPage] = Var(CharGenPage.BackgroundPage)
  val pageSignal = page.signal

  val showCharSheet: Var[Boolean] = Var(false)
  val showCharSheetSignal = showCharSheet.signal

  val advancePage: Observer[Unit] = page.updater { (p, _) =>
    p match
      case CharGenPage.BackgroundPage  => CharGenPage.PowerSourcePage
      case CharGenPage.PowerSourcePage => CharGenPage.ArchetypePage
      case CharGenPage.ArchetypePage   => CharGenPage.PersonalityPage
      case CharGenPage.PersonalityPage => CharGenPage.RedAbilityPage
      case CharGenPage.RedAbilityPage  => CharGenPage.RetconPage
      case CharGenPage.RetconPage      => CharGenPage.HealthPage
      case _                           => CharGenPage.BackgroundPage
  }
  val retreatPage: Observer[Unit] = page.updater { (p, _) =>
    p match
      case CharGenPage.HealthPage      => CharGenPage.RetconPage
      case CharGenPage.RetconPage      => CharGenPage.RedAbilityPage
      case CharGenPage.RedAbilityPage  => CharGenPage.PersonalityPage
      case CharGenPage.PersonalityPage => CharGenPage.ArchetypePage
      case CharGenPage.ArchetypePage   => CharGenPage.PowerSourcePage
      case CharGenPage.PowerSourcePage => CharGenPage.BackgroundPage
      case _                           => CharGenPage.BackgroundPage
  }

  val toggleCharSheet: Observer[Unit] = showCharSheet.updater { (b, _) => !b }
end CharGenModel
