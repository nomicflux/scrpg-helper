package scrpgHelper

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import upickle.default._
import com.raquo.laminar.api.L.{*, given}
import com.raquo.waypoint._

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

    val rollRoute = Route.static(Page.RollChart, root / "scrpg-helper" / "rollChart" / endOfSegments)
    val sceneRoute = Route.static(Page.SceneTracker, root / "scrpg-helper" / "sceneTracker" / endOfSegments)

    implicit val rw: ReadWriter[Page] = macroRW

    val router = new Router[Page](
        routes = List(rollRoute, sceneRoute),
        getPageTitle = _.toString,
        serializePage = page => write(page)(rw),
        deserializePage = pageStr => read(pageStr)(rw),
    )(
        popStateEvents = windowEvents(_.onPopState),
        owner = unsafeWindowOwner,
    )

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
        (page match
            case Page.RollChart => "Roll Dice"
            case Page.SceneTracker => "Scene Tracker"),
        disabled <-- router.currentPageSignal.map(_ == page),
        onClick --> { _event => router.pushState(page) }
      )
    end navButton

    def renderPage(): Element =
        div(
          className := "page",
          child <-- router.currentPageSignal.map(pageElement),
        )
    end renderPage

    def pageElement(page: Page): Element = page match
        case Page.RollChart => RollChart.rollChart()
        case Page.SceneTracker => SceneTracker.sceneTracker()
    end pageElement
end Main

enum Page:
    case RollChart, SceneTracker
end Page

final class Model:
end Model
