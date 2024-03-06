package scrpgHelper.components

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

case class SelectWithPrevChoice[A](items: List[A],
                                   toName: A => String):
  val lookup: Map[String, A] = items.map(a => (toName(a), a)).toMap

  def render(disabledChecker: Signal[(A, Option[A]) => Boolean],
             remover: Observer[A],
             adder: Observer[A]): Element =
    val prevChoice: Var[Option[A]] = Var(None)
    val prevChoiceSignal = prevChoice.signal
    val prevChoiceUpdater: Observer[Option[A]] = prevChoice.updater { (_, a) => a }

    def updateValue(name: String, prevA: Option[A]): Unit =
        val ma = lookup.get(name)
        prevA.foreach { a =>
          remover.onNext(a)
        }
        ma.foreach { a =>
          adder.onNext(a)
        }
        prevChoiceUpdater.onNext(ma)
    end updateValue

    select(
      className := "selector-with-prev-choice",
      option(
        value := "",
        ""
      ),
      items.map { a =>
        option(
          value := toName(a),
          disabled <-- disabledChecker.combineWith(prevChoiceSignal).map((f, mpc) => f(a, mpc)),
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
