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
          nextPageButton(character, model.pageSignal, model.advancePage),
          child <-- model.pageSignal.map(renderPage(_, character))
        )
    end charGen

    def nextPageButton(character: CharacterModel,
                       pageSignal: Signal[CharGenPage],
                       advancePage: Observer[Unit]): Element =
      button(
        tpe := "button",
        "Next Page",
        disabled <-- pageSignal.combineWith(character.validBackground).map((p, b) => p match {
                                      case CharGenPage.BackgroundPage => !b
                                      case CharGenPage.PowerSourcePage => true
                                    }),
        onClick --> { _ => advancePage.onNext(())}
      )
    end nextPageButton

    def renderPage(page: CharGenPage, character: CharacterModel): Element = page match
        case CharGenPage.BackgroundPage => RenderBackground.renderBackgrounds(character)
        case CharGenPage.PowerSourcePage => RenderPowerSource.renderPowerSources(character)
    end renderPage
end CharGen

enum CharGenPage:
    case BackgroundPage, PowerSourcePage
end CharGenPage

final class CharGenModel:
    val page: Var[CharGenPage] = Var(CharGenPage.BackgroundPage)
    val pageSignal = page.signal

    val advancePage: Observer[Unit] = page.updater { (p, _) => p match
      case CharGenPage.BackgroundPage => CharGenPage.PowerSourcePage
      case _ => CharGenPage.PowerSourcePage
    }
end CharGenModel
