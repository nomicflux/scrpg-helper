package scrpgHelper.chargen

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die

object RenderPowerSource:
    def renderPowerSources(character: CharacterModel): Element =
      div(
        h2("Power Source"),
      )
    end renderPowerSources
end RenderPowerSource
