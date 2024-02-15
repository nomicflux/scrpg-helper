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
    import scrpgHelper.scene.SceneTracker

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
        Page.values.map(navButton(_))
      )
    end renderNavBar

    def navButton(page: Page): Element =
      button(
        tpe := "button",
        className := "nav-button",
        page.render[String](() => "Roll Frequencies", () => "Scene Tracker"),
        disabled <-- model.pageSignal.map(_ == page),
        onClick --> { _event => model.updatePage(page) }
      )
    end navButton

    def renderPage(): Element =
        div(
          className := "page",
          child <-- model.pageSignal.map(_.render[Element](RollChart.rollChart, SceneTracker.sceneTracker),
          )
        )
    end renderPage
end Main

enum Page:
    case RollChart, SceneTracker

    def render[A](onRollChart: () => A,
                  onSceneTracker: () => A): A = this match
        case RollChart => onRollChart()
        case SceneTracker => onSceneTracker()
    end render
end Page

final class Model:
    val pageVar: Var[Page] = Var(Page.SceneTracker)
    val pageSignal = pageVar.signal

    def updatePage(page: Page): Unit =
      pageVar.update { _ => page }
    end updatePage
end Model
