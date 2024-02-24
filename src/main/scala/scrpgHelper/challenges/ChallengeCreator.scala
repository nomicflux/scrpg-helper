package scrpgHelper.challenges

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

object ChallengeCreator:
    import scrpgHelper.components.NumBox

    def challengeCreator(model: ChallengeCreatorModel): Element =
      div(
        h1("Challenge Creator"),
        renderChallengeCreator(model),
        renderChallenges(model),
      )
    end challengeCreator

    def renderChallenges(model: ChallengeCreatorModel): Element =
      div(
        children <-- model.challengesSignal.split(_.id){ (id, cs, s) => renderChallenge(model, cs, s) }
      )
    end renderChallenges

    def renderChallenge(model: ChallengeCreatorModel, box: ChallengeBox, signal: Signal[ChallengeBox]): Element =
      div(
        className := "challenge-box",
        (box.challenge match
          case CompoundChallenge.Simple(c) => renderSimpleChallenge(c,
                                                                    signal.map(_.challenge.forId(c.id)),
                                                                    model.observerForIds(box.id, c.id))
          case CompoundChallenge.And(cs) => renderSimultaneousChallenges(cs)
          case CompoundChallenge.AndThen(c, n) => renderSerialChallenges(c, n)
          case CompoundChallenge.Or(cs) => renderBranchingChallenges(cs)),
        box.timers.map(timer => renderTimer(
                         timer,
                         signal.map(box => box.timers.filter(_.getId() == timer.getId()).head),
                         model.timerObserver(timer.getId()),
                       )),
        div(
          className := "remove-challenge-box",
          "✘",
          onClick --> { _ => model.challenges.update { cs => cs.filter(_.id != box.id) } }
        )
      )
    end renderChallenge

    def renderSimpleChallenge(challenge: SimpleChallenge,
                              signal: Signal[Option[SimpleChallenge]],
                              observer: Observer[(Boolean, Int)]): Element =
      div(
        className := "simple-challenge-box",
        (1 to challenge.total).map(n => renderChallengeCheckbox(n, signal, observer)),
      )
    end renderSimpleChallenge

    def renderChallengeCheckbox(n: Int,
                                signal: Signal[Option[SimpleChallenge]],
                                observer: Observer[(Boolean, Int)]): Element =
      span(
        className := "challenge-box-checkbox-span",
        input(
          tpe := "checkbox",
          className := "challenge-box-checkbox",
          checked <-- signal.map(_.fold(false)(c => c.checked >= n)),
          onChange.mapToChecked --> { checked => observer.onNext(checked, n) }
        )
      )
    end renderChallengeCheckbox

    def renderTimer(timer: Timer, signal: Signal[Timer], observer: Observer[Boolean]): Element =
      timer match
        case st: Timer.SimpleTimer => renderSimpleTimer(st, signal, observer)
        case sct: Timer.StatusChangeTimer => renderStatusChangeTimer(sct, signal, observer)
    end renderTimer

    def renderSimpleTimer(timer: Timer.SimpleTimer, signal: Signal[Timer], observer: Observer[Boolean]): Element =
      div(
        className := "timer-box",
        (1 to timer.total).map(n => renderTimerCheckbox(n, signal, observer)),
      )
    end renderSimpleTimer

    def renderTimerCheckbox(n: Int,
                            signal: Signal[Timer],
                            observer: Observer[Boolean]): Element =
      span(
        className := "timer-checkbox-span",
        input(
          tpe := "checkbox",
          className := "timer-checkbox",
          checked <-- signal.map(_.getChecked() >= n),
          onChange.mapToChecked --> { checked => observer.onNext(checked) }
        )
      )
    end renderTimerCheckbox

    def renderStatusChangeTimer(timer: Timer.StatusChangeTimer, signal: Signal[Timer], observer: Observer[Boolean]): Element =
      div()
    end renderStatusChangeTimer

    def renderSimultaneousChallenges(challenges: List[CompoundChallenge]): Element =
      div(

      )
    end renderSimultaneousChallenges

    def renderSerialChallenges(challenge: CompoundChallenge, nextChallenge: CompoundChallenge): Element =
      div(

      )
    end renderSerialChallenges

    def renderBranchingChallenges(challenges: List[CompoundChallenge]): Element =
      div(

      )
    end renderBranchingChallenges

    def renderChallengeCreator(model: ChallengeCreatorModel): Element =
      val challengeCheckboxes: Var[Int] = Var(1)
      val timerCheckboxes: Var[Int] = Var(0)

      val challengeCheckboxesSignal = challengeCheckboxes.signal
      val timerCheckboxesSignal = timerCheckboxes.signal

      div(
        className := "challenge-box-creator",
        NumBox(
          challengeCheckboxesSignal,
          challengeCheckboxes.updater { (n, x) => n + x },
          challengeCheckboxes.updater { (_, m) => m }
        ).withLabel("# of Challenge Checkboxes")
          .withSpanClassName("simple-challenge-creator")
          .withMinVal(1)
          .render(),
        NumBox(
          timerCheckboxesSignal,
          timerCheckboxes.updater { (n, x) => n + x },
          timerCheckboxes.updater { (_, m) => m }
        ).withLabel("# of Timer Checkboxes")
          .withSpanClassName("simple-timer-creator")
          .withMinVal(0)
          .render(),
        button(
          tpe := "button",
          "Create Challenge",
          onClick.compose(_.withCurrentValueOf(challengeCheckboxesSignal, timerCheckboxesSignal)) --> { case (_, c, t) =>
            model.createSimpleChallenge(c, Some(t).filter(_ >= 1))
          }
        )
      )
    end renderChallengeCreator
end ChallengeCreator

final class ChallengeCreatorModel:
    val challenges: Var[List[ChallengeBox]] = Var(List())
    val challengesSignal = challenges.signal

    def createSimpleChallenge(challengeChecks: Int, timerChecks: Option[Int]): Unit =
      challenges.update { cs =>
        val c = ChallengeBox.createSimpleChallengeBox(challengeChecks)
        cs :+  timerChecks.fold(c)(n => c.addTimer(Timer.createSimpleTimer(n)))
      }
    end createSimpleChallenge

    def observerForIds(boxId: ChallengeBoxId, challengeId: SimpleChallengeId): Observer[(Boolean, Int)] =
      challenges.updater { case (boxes, (checked, n)) =>
        boxes.map { box =>
          if(box.id == boxId) {
            box.updateAtId(challengeId, challenge => Some(challenge.checkAtBox(checked, n)))
          } else {
            Some(box)
          }
        }.collect { case Some(box) => box }
      }
    end observerForIds

    def timerObserver(timerId: TimerId): Observer[Boolean] =
      challenges.updater { case (boxes, checked) =>
        boxes.map { box =>
          box.updateTimer(timerId, timer => Some(if checked then timer.checkBox() else timer.uncheckBox()))
        }
      }
    end timerObserver
end ChallengeCreatorModel
