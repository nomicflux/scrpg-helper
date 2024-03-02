package scrpgHelper.challenges

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.*

object RenderTimer:
  import scrpgHelper.components.NameBox
  import scrpgHelper.status.Status

  def renderTimer(
      timer: Timer,
      boxSignal: Signal[ChallengeBox],
      signal: Signal[Option[Timer]],
      statusSignal: Signal[Option[Status]],
      checkboxObserver: Observer[Boolean],
      nameObserver: Observer[String]
  ): Element =
    timer match
      case st: Timer.SimpleTimer =>
        renderSimpleTimer(st, boxSignal, signal, checkboxObserver, nameObserver)
      case sct: Timer.StatusChangeTimer =>
        renderStatusChangeTimer(sct, boxSignal, signal, statusSignal, nameObserver)
  end renderTimer

  def renderSimpleTimer(
      timer: Timer.SimpleTimer,
      boxSignal: Signal[ChallengeBox],
      signal: Signal[Option[Timer]],
      checkboxObserver: Observer[Boolean],
      nameObserver: Observer[String]
  ): Element =
    div(
      className := "timer-box",
      div(
        className := "name",
        NameBox(timer.name.getOrElse(""),
                signal.map(_.flatMap(_.getName())),
                nameObserver).render(),
      ),
      div(
        className := "shown-checkboxes",
        (1 to timer.total).map(n => renderTimerCheckbox(n, signal, checkboxObserver))
      ),
      div(
        className := "obfuscated-checkboxes",
        span(
          input(
            tpe := "checkbox",
            className := "challenge-box-obfuscated",
            onClick --> { _ => checkboxObserver.onNext(true) }
          )
        )
      )
    )
  end renderSimpleTimer

  def renderTimerCheckbox(
      n: Int,
      signal: Signal[Option[Timer]],
      observer: Observer[Boolean]
  ): Element =
    span(
      className := "timer-checkbox-span",
      input(
        tpe := "checkbox",
        className := "timer-checkbox",
        checked <-- signal.map(_.fold(false)(_.getChecked() >= n)),
        onChange.mapToChecked --> { checked => observer.onNext(checked) }
      )
    )
  end renderTimerCheckbox

  def renderStatusChangeTimer(
      timer: Timer.StatusChangeTimer,
      boxSignal: Signal[ChallengeBox],
      signal: Signal[Option[Timer]],
      statusSignal: Signal[Option[Status]],
      nameObserver: Observer[String]
  ): Element =
    div(
      className := "timer-box",
      div(
        className := "name",
        NameBox(timer.name.getOrElse(""),
                signal.map(_.flatMap(_.getName())),
                nameObserver).render(),
      ),
      div(
        className := "timer-div",
        className := (if timer.onStatus.contains(Status.Yellow) then "timer-div-yellow" else ""),
        className := (if timer.onStatus.contains(Status.Red) then "timer-div-red" else ""),
        className <-- statusSignal.map { s => if timer.completed(s) then "timer-div-timeout" else "timer-div-active" },
        child.text <-- statusSignal.map { s => if timer.completed(s) then "Too late!" else s"Until ${timer.statusString()}"}
      )
    )
  end renderStatusChangeTimer
end RenderTimer
