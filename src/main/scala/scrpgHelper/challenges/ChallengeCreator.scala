package scrpgHelper.challenges

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.*

object ChallengeCreator:
  import scrpgHelper.components.NameBox
  import scrpgHelper.components.NumBox

  def challengeCreator(model: ChallengeCreatorModel): Element =
    div(
      h1("Challenge Creator (Experimental)"),
      renderChallengeCreator(model),
      RenderChallenge.renderChallenges(model)
    )
  end challengeCreator

  enum Action:
    case Increase, Decrease
  end Action

  def challengeCheckboxCreatorRow(
      id: Int,
      nSignal: Signal[(Option[String], Int)],
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
          NumBox(
            nSignal.map(_._2),
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
      f: A => Int,
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
          o.contramap(h => n => g(h(f(n)))),
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
    val timerCheckboxes: Var[List[(Option[String], Int)]] = Var(List((None, 1)))

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
          identity,
          identity,
          { (s, o) => List() }
        ),
        challengeCheckboxCreator[Int](
          "# of Timer Checkboxes",
          0,
          timerCheckboxesSignal,
          timerCheckboxes.updater { (ts, f) => f(ts) },
          identity,
          identity,
          { (s, o) => List() }
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
end ChallengeCreator

final class ChallengeCreatorModel:
  val challenges: Var[List[ChallengeBox]] = Var(List())
  val challengesSignal = challenges.signal

  def createChallenge(
      name: String,
      challengeChecks: List[(Option[String], Int)],
      timerChecks: List[(Option[String], Int)]
  ): Unit =
    challenges.update { cs =>
      val c = ChallengeBox.createSimultaneousChallengeBox(name, challengeChecks)
      cs :+ timerChecks.foldLeft(c)((d, n) =>
        d.addTimer(Timer.createSimpleTimer(n._1, n._2))
      )
    }
  end createChallenge

  def observerForIds[A](
      boxId: ChallengeBoxId,
      challengeId: SimpleChallengeId,
      f: (SimpleChallenge, A) => SimpleChallenge
  ): Observer[A] =
    challenges.updater { case (boxes, a) =>
      boxes
        .map { box =>
          if (box.id == boxId) {
            box.updateAtId(challengeId, challenge => f(challenge, a))
          } else {
            box
          }
        }
    }
  end observerForIds

  def checkboxObserver(
      boxId: ChallengeBoxId,
      challengeId: SimpleChallengeId
  ): Observer[(Boolean, Int)] =
    observerForIds(
      boxId,
      challengeId,
      (c, checkedAt) => c.checkAtBox(checkedAt._1, checkedAt._2)
    )
  end checkboxObserver

  def escalationObserver(
      boxId: ChallengeBoxId,
      challengeId: SimpleChallengeId
  ): Observer[Unit] =
    observerForIds(boxId, challengeId, (c, _) => c.toggleEscalate())
  end escalationObserver

  def challengeNameObserver(
      boxId: ChallengeBoxId,
      challengeId: SimpleChallengeId
  ): Observer[String] =
    observerForIds(boxId, challengeId, (c, name) => c.copy(name = Some(name)))
  end challengeNameObserver

  def timerCheckboxObserver(timerId: TimerId): Observer[Boolean] =
    challenges.updater { case (boxes, checked) =>
      boxes.map { box =>
        box.updateTimer(
          timerId,
          timer => if checked then timer.checkBox() else timer.uncheckBox()
        )
      }
    }
  end timerCheckboxObserver

  def timerNameObserver(timerId: TimerId): Observer[String] =
    challenges.updater { case (boxes, name) =>
      boxes.map { box =>
        box.updateTimer(
          timerId,
          timer => timer.changeName(name)
        )
      }
    }
  end timerNameObserver

  def nameObserver(boxId: ChallengeBoxId): Observer[String] =
    challenges.updater { (boxes, name) =>
      boxes.map { box =>
        if (box.id == boxId) {
          box.copy(name = name)
        } else {
          box
        }
      }
    }
  end nameObserver
end ChallengeCreatorModel
