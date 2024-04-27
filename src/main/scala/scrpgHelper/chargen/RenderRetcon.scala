package scrpgHelper.chargen

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die

object RenderRetcon:
  def renderRetcon(character: CharacterModel): Element =
    div(
      className := "retcon-section choice-section",
      h2("Retcon"),
      div(
        "Retcons not supported yet."
      )
    )
end RenderRetcon
