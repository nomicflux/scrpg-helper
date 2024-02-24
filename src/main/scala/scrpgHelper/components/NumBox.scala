package scrpgHelper.components

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

case class NumBox(label: Option[String],
                  spanClassName: Option[String],
                  inputClassName: Option[String],
                  inputSize: Option[Int],
                  signal: Signal[Int],
                  incrementer: Observer[Int],
                  observer: Observer[Int]):
    def withLabel(s: String): NumBox =
      copy(label = Some(s))
    end withLabel

    def withSpanClassName(s: String): NumBox =
      copy(spanClassName = Some(s))
    end withSpanClassName

    def withInputClassName(s: String): NumBox =
      copy(inputClassName = Some(s))
    end withInputClassName

    def withSize(n: Int): NumBox =
      copy(inputSize = Some(n))
    end withSize

    def render(): Element =
        span(
          className := spanClassName.getOrElse(""),
          span(label.getOrElse("")),
          button(
            tpe := "button",
            className := "spinner",
            "-",
            onClick --> { _ =>  incrementer.onNext(-1) }
          ),
          input(
            tpe := "text",
            size := inputSize.getOrElse(2),
            inputMode := "numeric",
            className := inputClassName.getOrElse(""),
            controlled(
              value <-- signal.map(_.toString),
              onInput.mapToValue.map(_.toIntOption).map(_.filter(_ >= 0)).collect {
                case Some(n)  => n
              } --> observer
            )
          ),
          button(
            tpe := "button",
            className := "spinner",
            "+",
            onClick --> { _ =>  incrementer.onNext(1) }
          ),
        )
    end render
end NumBox

object NumBox:
    def apply(signal: Signal[Int],
              incrementer: Observer[Int],
              observer: Observer[Int]): NumBox =
      new NumBox(None, None, None, None, signal, incrementer, observer)
    end apply
end NumBox
