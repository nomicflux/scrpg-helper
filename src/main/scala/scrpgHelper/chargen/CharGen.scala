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
          child <-- model.pageSignal.map(renderPage(_, character))
        )
    end charGen

    def renderPage(page: CharGenPage, character: CharacterModel): Element = page match
        case CharGenPage.BackgroundPage => RenderBackground.renderBackgrounds(character)
    end renderPage
end CharGen

enum CharGenPage:
    case BackgroundPage
end CharGenPage

final class CharGenModel:
    val page: Var[CharGenPage] = Var(CharGenPage.BackgroundPage)
    val pageSignal = page.signal
end CharGenModel
