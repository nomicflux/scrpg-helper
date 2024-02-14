package scrpgHelper

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

@main
def SCRPGHelper(): Unit =
  renderOnDomContentLoaded(
    dom.document.getElementById("app"),
    Main.appElement()
  )

object Main:
    import scrpgHelper.rolls.RollChart

    val model = new Model

    def appElement(): Element =
      div(
        renderNavBar(),
        renderPage(),
      )
    end appElement

    def renderNavBar(): Element =
      div(
        className := "nav",
        navButton(Page.RollChart)
      )
    end renderNavBar

    def navButton(page: Page): Element =
      button(
        tpe := "button",
        className := "nav-button",
        page.render[String](() => "Roll Frequencies"),
        disabled <-- model.pageSignal.map(_ == page),
        onClick --> { _event => model.updatePage(page) }
      )
    end navButton

    def renderPage(): Element =
        div(
          className := "page",
          child <-- model.pageSignal.map(_.render[Element](RollChart.rollChart))
        )
    end renderPage
end Main

enum Page:
    case RollChart

    def render[A](onRollChart: () => A): A = this match
        case RollChart => onRollChart()
end Page

final class Model:
    val pageVar: Var[Page] = Var(Page.RollChart)
    val pageSignal = pageVar.signal

    def updatePage(page: Page): Unit =
      pageVar.update { _ => page }
    end updatePage
end Model
