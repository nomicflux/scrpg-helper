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
            prevPageButton(character, model.pageSignal, model.retreatPage),
            nextPageButton(character, model.pageSignal, model.advancePage),
          ),
          renderPage(model.pageSignal, character)
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

    def prevPageButton(character: CharacterModel,
                       pageSignal: Signal[CharGenPage],
                       retreatPage: Observer[Unit]): Element =
      button(
        tpe := "button",
        "Previous Page",
        disabled <-- pageSignal.map(p => p match {
                                      case CharGenPage.BackgroundPage => true
                                      case _ => false
                                    }),
        onClick --> { _ => retreatPage.onNext(())}
      )
    end prevPageButton

    def renderPage(pageSignal: Signal[CharGenPage], character: CharacterModel): Element =
      div(
        div(
          className <-- pageSignal.map(p => if p == CharGenPage.BackgroundPage then "" else "hidden"),
          RenderBackground.renderBackgrounds(character),
        ),
        div(
          className <-- pageSignal.map(p => if p == CharGenPage.PowerSourcePage then "" else "hidden"),
          RenderPowerSource.renderPowerSources(character),
        )
      )
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
    val retreatPage: Observer[Unit] = page.updater { (p, _) => p match
      case CharGenPage.PowerSourcePage => CharGenPage.BackgroundPage
      case _ => CharGenPage.BackgroundPage
    }
end CharGenModel
