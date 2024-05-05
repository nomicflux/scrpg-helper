package scrpgHelper.chargen

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die

object RollComponent:
  def renderRollButton(rollTrigger: Observer[List[Die]], dieSource: Signal[List[Die]]): Element =
    div(
      button(
        tpe := "button",
        "Guided Selection 🎲",
        onClick
          .compose(
            _.withCurrentValueOf(
              dieSource
            )
          ) --> { (_, dice) =>
          rollTrigger.onNext(dice)
        }
      )
    )
  end renderRollButton

  def renderShownToggle(
      rollsSignal: Signal[Option[Set[Int]]],
      shown: Signal[Boolean],
      shownToggle: Observer[Unit],
      things: String,
  ): Element =
    div(
      className <-- rollsSignal.map(rs => if rs.isEmpty then "hidden" else ""),
      button(
        tpe := "button",
        disabled <-- rollsSignal.map(_.isEmpty),
        child.text <-- shown.map(b => if b then s"Hide Unrolled ${things}" else s"Show All ${things}"),
        onClick --> { _ => shownToggle.onNext(()) }
      )
    )
  end renderShownToggle
end RollComponent
