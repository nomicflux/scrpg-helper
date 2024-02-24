package scrpgHelper.components

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

case class NumBox(label: Option[String],
                  spanClassName: Option[String],
                  inputClassName: Option[String],
                  inputSize: Option[Int],
                  maxVal: Option[Int],
                  minVal: Option[Int],
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

    def withMaxVal(n: Int): NumBox =
      copy(maxVal = Some(n))
    end withMaxVal

    def withMinVal(n: Int): NumBox =
      copy(minVal = Some(n))
    end withMinVal

    def inBounds(n: Int): Boolean =
      maxVal.fold(true)(_ >= n) && minVal.fold(true)(_ <= n)
    end inBounds

    def render(): Element =
        span(
          className := spanClassName.getOrElse(""),
          span(label.getOrElse("")),
          button(
            tpe := "button",
            className := "spinner",
            disabled <-- signal.map(n => !inBounds(n - 1)),
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
              onInput.mapToValue.map(_.toIntOption).map(_.filter(n => inBounds(n))).collect {
                case Some(n)  => n
              } --> observer
            )
          ),
          button(
            tpe := "button",
            className := "spinner",
            disabled <-- signal.map(n => !inBounds(n + 1)),
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
      new NumBox(None, None, None, None, None, None, signal, incrementer, observer)
    end apply
end NumBox
