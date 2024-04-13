package scrpgHelper.chargen
import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die

object RenderArchetype:

  def renderArchetypes(character: CharacterModel): Element =
    div(
      h1("Archetype")
    )
end RenderArchetype
