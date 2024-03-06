package scrpgHelper.components

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

case class SelectWithPrevChoice[A](items: List[A],
                                   toName: A => String):
  val lookup: Map[String, A] = items.map(a => (toName(a), a)).toMap

  def render(signal: Signal[Set[String]],
             remover: Observer[A],
             adder: Observer[A]): Element =
    val prevChoice: Var[Option[A]] = Var(None)
    val prevChoiceSignal = prevChoice.signal
    val prevChoiceUpdater: Observer[A] = prevChoice.updater { (_, a) =>
      Some(a)
    }

    def updateValue(name: String, prevA: Option[A]): Unit =
        val ma = lookup.get(name)
        prevA.foreach { a =>
          remover.onNext(a)
        }
        ma.foreach { a =>
          adder.onNext(a)
          prevChoiceUpdater.onNext(a)
        }
    end updateValue

    select(
      option(
        value := "",
        ""
      ),
      items.map { a =>
        option(
          value := toName(a),
          disabled <-- signal.map(_.contains(toName(a))),
          toName(a)
        )
      },
      onFocus.mapToValue.compose(_.withCurrentValueOf(prevChoiceSignal)) --> { (n, pA) => updateValue(n, pA) },
      onChange.mapToValue.compose(_.withCurrentValueOf(prevChoiceSignal)) --> { (n, pA) => updateValue(n, pA) }
    )

  end render
end SelectWithPrevChoice

object SelectWithPrevChoice:
    def apply[A](items: List[A], toName: A => String): SelectWithPrevChoice[A] =
        new SelectWithPrevChoice[A](items, toName)
    end apply
end SelectWithPrevChoice
