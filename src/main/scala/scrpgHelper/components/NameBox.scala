package scrpgHelper.components

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

case class NameBox(
  origName: String,
  signal: Signal[Option[String]],
  observer: Observer[String]
):
  def render(): Element =
    val editing: Var[Boolean] = Var(false)
    val editingSignal = editing.signal

    val nameVar: Var[String] = Var(origName)
    val nameSignal = nameVar.signal

    def updateName(newName: String): Unit =
      observer.onNext(newName)
      editing.update(_ => false)
    end updateName

    div(
      className := "name-box",
      onClick --> { _ => editing.update(_ => true) },
      input(
        tpe := "text",
        className <-- editingSignal.map(e => if e then "" else "hidden"),
        value <-- nameSignal,
        onBlur.compose(_.withCurrentValueOf(nameSignal)) --> { (_, newName) =>
          updateName(newName)
        },
        onInput.mapToValue --> nameVar,
        onKeyPress.compose(_.withCurrentValueOf(nameSignal)) --> {
          (event, newName) =>
            if (event.key == "Enter") {
              updateName(newName)
            }
        }
      ),
      h3(
        className <-- editingSignal.map(e => if e then "hidden" else ""),
        child.text <-- signal.map(_.getOrElse(""))
      )
    )
  end render
end NameBox

object NameBox:
    def apply(origName: String,
              signal: Signal[Option[String]],
              observer: Observer[String]): NameBox =
      new NameBox(origName, signal, observer)
    end apply
end NameBox
