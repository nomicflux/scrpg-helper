package scrpgHelper.chargen

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die

object RenderHealth:
  def renderHealth(character: CharacterModel): Element =
    div(
      className := "health-section choice-section",
      h2("Health"),
      div(
      )
    )
end RenderHealth
