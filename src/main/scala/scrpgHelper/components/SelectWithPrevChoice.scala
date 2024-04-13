package scrpgHelper.components

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

case class SelectWithPrevChoice[A](items: Signal[List[A]],
                                   toName: A => String):
  val lookup: Signal[Map[String, A]] = items.map(_.map(a => (toName(a), a)).toMap)

  def render(disabledChecker: Signal[(A, Option[A]) => Boolean],
             remover: Observer[A],
             adder: Observer[A]): Element =
    val prevChoice: Var[Option[A]] = Var(None)
    val prevChoiceSignal = prevChoice.signal
    val prevChoiceUpdater: Observer[Option[A]] = prevChoice.updater { (_, a) => a }

    def updateValue(name: String, prevA: Option[A], lookedUp: Option[A]): Unit =
        prevA.foreach { a =>
          remover.onNext(a)
        }
        lookedUp.foreach { a =>
          adder.onNext(a)
        }
        prevChoiceUpdater.onNext(lookedUp)
    end updateValue

    select(
      className := "selector-with-prev-choice",
      option(
        value := "",
        ""
      ),
      children <-- items.split(toName(_)) { (name, a, _) =>
        option(
          value := name,
          disabled <-- disabledChecker.combineWith(prevChoiceSignal).map((f, mpc) => f(a, mpc)),
          name
        )
      },
      onFocus.mapToValue.compose(_.withCurrentValueOf(prevChoiceSignal, lookup)) --> { (n, pA, m) => updateValue(n, pA, m.get(n)) },
      onChange.mapToValue.compose(_.withCurrentValueOf(prevChoiceSignal, lookup)) --> { (n, pA, m) => updateValue(n, pA, m.get(n)) }
    )

  end render
end SelectWithPrevChoice

object SelectWithPrevChoice:
    def apply[A](items: List[A], toName: A => String): SelectWithPrevChoice[A] =
        new SelectWithPrevChoice[A](Signal.fromValue(items), toName)
    end apply

    def forSignal[A](items: Signal[List[A]], toName: A => String): SelectWithPrevChoice[A] =
        new SelectWithPrevChoice[A](items, toName)
    end forSignal
end SelectWithPrevChoice
