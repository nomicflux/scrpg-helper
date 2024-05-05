package scrpgHelper.chargen

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die
import scrpgHelper.status.Status

object RenderHealth:
  def renderHealth(character: CharacterModel): Element =
    div(
      className := "health-section choice-section",
      h2("Health"),
      div(
        renderHealthElements(character)
      )
    )

  def renderHealthElements(character: CharacterModel): Element =
    table(
      className := "health-table",
      tr(
        td("Base value:"),
        td(8)
      ),
      tr(
        td("Red Status Die:"),
        td(
          child.text <-- character.redZoneHealth.map(_.fold("")(_.toString))
        )
      ),
      tr(
        td("Maximum Value of Athletic Power or Mental Quality:"),
        td(
          child.text <-- character.powerQualityHealth.map(_.toString)
        )
      ),
      tr(
        td("Roll or 4:"),
        td(
          button(
            tpe := "button",
            disabled <-- character.healthSignal.map(_.isDefined),
            "Roll",
            onClick.compose(_.withCurrentValueOf(character.redZoneHealth, character.powerQualityHealth)) --> { (_ev, rzh, pqh) =>
              character.calcHealth.onNext(8 + rzh.getOrElse(4) + pqh + Die.d(8).roll())
            }
          ),
          " - or  -",
          button(
            tpe := "button",
            disabled <-- character.healthSignal.map(_.isDefined),
            "Four",
            onClick.compose(_.withCurrentValueOf(character.redZoneHealth, character.powerQualityHealth)) --> { (_ev, rzh, pqh) =>
              character.calcHealth.onNext(8 + rzh.getOrElse(4) + pqh + 4)
            }
          ),
        )
      ),
      tfoot(
        tr(
          td("Total Health:"),
          td(
            child.text <-- character.healthSignal.map(_.fold("")(_.toString))
          )
        )
      )
    )
end RenderHealth
