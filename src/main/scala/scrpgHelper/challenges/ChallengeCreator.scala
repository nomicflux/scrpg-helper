package scrpgHelper.challenges

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.*

object ChallengeCreator:
  import scrpgHelper.components.NameBox
  import scrpgHelper.components.NumBox
  import scrpgHelper.status.Status

  def challengeCreator(model: ChallengeCreatorModel): Element =
    val noStatusSignal: Signal[Option[Status]] = Signal.fromValue[Option[Status]](None)
    div(
      h1("Challenge Creator (Experimental)"),
      renderChallengeCreator(model),
      RenderChallenge.renderChallenges(model, noStatusSignal)
    )
  end challengeCreator

  enum Action:
    case Increase, Decrease
  end Action

  def challengeCheckboxCreatorRow(
      id: Int,
      nSignal: Signal[(Option[String], Option[Int])],
      nObserver: Observer[Int => Int],
      els: List[Element],
      nameObserver: Observer[Option[String]]
  ): Element =
    tr(
      td(
        div(
          className := "creator-extra-els",
          els
        )
      ),
      td(
        div(
          className <-- nSignal.map(n => n._2.fold("hidden")(_ => "shown")),
          NumBox(
            nSignal.map(_._2.getOrElse(0)),
            nObserver.contramap(n => x => n + x),
            nObserver.contramap(n => m => m)
          ).withSpanClassName("simple-challenge-creator")
            .withMinVal(1)
            .render()
        )
      ),
      td(
        input(
          tpe := "input",
          value <-- nSignal.map(_._1.getOrElse("")),
          onInput.mapToValue --> { newName =>
            nameObserver.onNext(Some(newName))
          }
        )
      )
    )
  end challengeCheckboxCreatorRow

  def challengeCheckboxCreator[A](
      header: String,
      minNum: Int,
      signal: Signal[List[(Option[String], A)]],
      observer: Observer[
        List[(Option[String], A)] => List[(Option[String], A)]
      ],
      f: A => Option[Int],
      g: Int => A,
      els: (Signal[(Option[String], A)], Observer[A => A]) => List[Element]
  ): Element =
    val rowsObserver = observer.contramap(a =>
      ls => {
        a match
          case Action.Increase => ls :+ (None, g(1))
          case Action.Decrease => ls.init
      }
    )

    table(
      className := "row-changer",
      thead(
        td(
          colSpan := 2,
          h4(header)
        ),
        td(
          div(
            "+/- Rows",
            className := "row-changer-buttons",
            button(
              tpe := "button",
              className := "decrease-button size-button",
              disabled <-- signal.map(_.size <= minNum),
              "-",
              onClick --> { _ => rowsObserver.onNext(Action.Decrease) }
            ),
            button(
              tpe := "button",
              className := "increase-button size-button",
              "+",
              onClick --> { _ => rowsObserver.onNext(Action.Increase) }
            )
          )
        )
      ),
      children <-- signal.map(_.zipWithIndex).split(_._2) { case (id, n, sn) =>
        val s = sn.map(_._1)
        val o: Observer[A => A] = observer.contramap(f =>
            ls =>
              ls.zipWithIndex.map { case (n, i) =>
                if i == id then (n._1, f(n._2)) else n
              }
          )
        challengeCheckboxCreatorRow(
          id,
          s.map(n => (n._1, f(n._2))),
          o.contramap(h => n => g(h(f(n).getOrElse(0)))),
          els(s, o),
          observer.contramap(newName =>
            ls =>
              ls.zipWithIndex.map { case (n, i) =>
                if i == id then (newName, n._2) else n
              }
          )
        )
      }
    )
  end challengeCheckboxCreator

  def renderChallengeCreator(model: ChallengeCreatorModel): Element =
    val name: Var[String] = Var("")
    val challengeCheckboxes: Var[List[(Option[String], Int)]] = Var(
      List((None, 1))
    )
    val timerCheckboxes: Var[List[(Option[String], PreTimer)]] = Var(List((None, PreTimer.fromInt(1))))

    val nameSignal = name.signal
    val challengeCheckboxesSignal = challengeCheckboxes.signal
    val timerCheckboxesSignal = timerCheckboxes.signal

    div(
      className := "challenge-box-creator",
      div(
        span("Name"),
        input(
          tpe := "text",
          value <-- nameSignal,
          onInput.mapToValue --> { n => name.update { _ => n } }
        )
      ),
      div(
        className := "rows-setup",
        challengeCheckboxCreator[Int](
          "# of Challenge Checkboxes",
          1,
          challengeCheckboxesSignal,
          challengeCheckboxes.updater { (cs, f) => f(cs) },
          Some(_),
          identity,
          { (s, o) => List() }
        ),
        challengeCheckboxCreator[PreTimer](
          "Status / # of Timer Checkboxes",
          0,
          timerCheckboxesSignal,
          timerCheckboxes.updater { (ts, f) => f(ts) },
          PreTimer.toInt(_),
          PreTimer.fromInt(_),
          { (s, o) => List(renderTimerButton(s, o)) }
        )
      ),
      button(
        tpe := "button",
        "Create Challenge",
        onClick.compose(
          _.withCurrentValueOf(
            nameSignal,
            challengeCheckboxesSignal,
            timerCheckboxesSignal
          )
        ) --> { case (_, n, cs, ts) =>
          model.createChallenge(n, cs, ts)
        }
      )
    )
  end renderChallengeCreator

  def renderTimerButton(signal: Signal[(Option[String], PreTimer)],
                        observer: Observer[PreTimer => PreTimer]): Element =
    def rotateTimer(timer: PreTimer): PreTimer = timer match
      case PreTimer.WithNum(_) => PreTimer.WithStatus(Status.Yellow)
      case PreTimer.WithStatus(Status.Yellow) => PreTimer.WithStatus(Status.Red)
      case PreTimer.WithStatus(Status.Red) => PreTimer.WithNum(1)
      case _ => PreTimer.WithNum(1)
    end rotateTimer

    div(
      className := "timer-button-div",
      button(
        tpe := "button",
        className := "timer-button",
        className <-- signal.map(n => n._2 match
                                   case PreTimer.WithNum(_) => "timer-button-number"
                                   case PreTimer.WithStatus(Status.Yellow) => "timer-button-yellow"
                                   case PreTimer.WithStatus(Status.Red) => "timer-button-red"
                                   case _ => "timer-button-illegal"
        ),
        child.text <-- signal.map(n => n._2 match
                                   case PreTimer.WithNum(_) => "# Boxes"
                                   case PreTimer.WithStatus(Status.Yellow) => "On Yellow"
                                   case PreTimer.WithStatus(Status.Red) => "On Red"
                                   case _ => "???"
        ),
        onClick --> { _ => observer.onNext(p => rotateTimer(p)) }
      )
    )
  end renderTimerButton
end ChallengeCreator
