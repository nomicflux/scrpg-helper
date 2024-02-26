package scrpgHelper.challenges

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

object ChallengeCreator:
    import scrpgHelper.components.NumBox

    def challengeCreator(model: ChallengeCreatorModel): Element =
      div(
        h1("Challenge Creator (Experimental)"),
        renderChallengeCreator(model),
        renderChallenges(model),
      )
    end challengeCreator

    def renderChallenges(model: ChallengeCreatorModel): Element =
      div(
        className := "challenge-creator-section",
        children <-- model.challengesSignal.split(_.id){ (id, cs, s) => renderChallenge(model, cs, s) }
      )
    end renderChallenges

    def renderChallenge(model: ChallengeCreatorModel,
                        origBox: ChallengeBox,
                        signal: Signal[ChallengeBox]): Element =
      val nameObserver = model.nameObserver(origBox.id)

      div(
        className := "challenge-box challenge-creator-holder",
        className <-- signal.map(box => s"challenge-box-completed-${box.completed()}"),
        className <-- signal.map(box => s"challenge-box-timeout-${box.timeout(None)}"),
        className <-- signal.map(box => s"challenge-box-shown-${box.shown}"),
        renderNameBox(origBox.name, signal, nameObserver),
        (origBox.challenge match
          case CompoundChallenge.Simple(c) => renderSimpleChallenge(c,
                                                                    signal.map(_.challenge.forId(c.id)),
                                                                    model.escalationObserver(origBox.id, c.id),
                                                                    model.checkboxObserver(origBox.id, c.id))
          case CompoundChallenge.And(cs) => renderSimultaneousChallenges(cs)
          case CompoundChallenge.AndThen(c, n) => renderSerialChallenges(c, n)
          case CompoundChallenge.Or(cs) => renderBranchingChallenges(cs)),
        origBox.timers.map(timer => renderTimer(
                         timer,
                         signal,
                         signal.map(box => box.timers.filter(_.getId() == timer.getId()).headOption),
                         model.timerObserver(timer.getId()),
                       )),
        div(
          className := "remove-challenge-box",
          "✘",
          onClick --> { _ => model.challenges.update { cs => cs.filter(_.id != origBox.id) } }
        ),
        div(
          className := "show-challenge-box",
          span(
            "Show checkboxes on Scene Tracker:"
          ),
          span(
            className <-- signal.map(box => s"shown-challenge-box-${box.shown}"),
            "👁",
          ),
          onClick --> { _ =>
            model.challenges.update { cs => cs.map { box =>
                                       if(box.id == origBox.id) {
                                         box.toggleShown()
                                       } else { box }
                                     }
              }
          }
        ),
      )
    end renderChallenge

    def renderNameBox(origName: String, boxSignal: Signal[ChallengeBox], observer: Observer[String]): Element =
      val editing: Var[Boolean] = Var(false)
      val editingSignal = editing.signal

      val nameVar: Var[String] = Var(origName)
      val nameSignal = nameVar.signal

      def updateName(newName: String): Unit =
        observer.onNext(newName)
        editing.update(_ => false)
      end updateName

      div(
        className := "name-box",
        onClick --> { _ => editing.update(_ => true) },
        input(
          tpe := "text",
          className <-- editingSignal.map(e => if e then "" else "hidden"),
          value <-- nameSignal,
          onBlur.compose(_.withCurrentValueOf(nameSignal)) --> { (_, newName) =>
            updateName(newName)
          },
          onInput.mapToValue --> nameVar,
          onKeyPress.compose(_.withCurrentValueOf(nameSignal)) --> { (event, newName) =>
            if(event.key == "Enter") {
              updateName(newName)
            }
          },
        ),
        h3(
          className <-- editingSignal.map(e => if e then "hidden" else ""),
          child.text <-- boxSignal.map(_.name),
        ),
      )
    end renderNameBox

    def renderSimpleChallenge(challenge: SimpleChallenge,
                              signal: Signal[Option[SimpleChallenge]],
                              escalationObserver: Observer[Unit],
                              checkboxObserver: Observer[(Boolean, Int)]): Element =
      div(
        className := "simple-challenge-box",
        className <-- signal.map(mc => mc.fold("")(c => s"escalated-${c.escalated}")),
        div(
          className := "checkbox-container escalation-checkbox-container",
          span(
            child.text <-- signal.map(mc => mc.fold("")(c => if c.escalated then "De-escalate" else "Escalate")),
            " Twists:"
          ),
          input(
            tpe := "checkbox",
            className := "escalate-checkbox",
            checked <-- signal.map(mc => mc.fold(false)(_.escalated)),
          ),
          onClick --> { _ =>
            escalationObserver.onNext(())
          }
        ),
        div(
          className := "shown-checkboxes",
          (1 to challenge.total).map(n => renderChallengeCheckbox(n, signal, checkboxObserver))
        ),
        div(
          className := "obfuscated-checkboxes",
          span(
            input(
              tpe := "checkbox",
              className := "challenge-box-obfuscated",
              onClick --> { _ => checkboxObserver.onNext(true, 0) }
            )
          )
        ),
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

    def renderTimer(timer: Timer, boxSignal: Signal[ChallengeBox], signal: Signal[Option[Timer]], observer: Observer[Boolean]): Element =
      timer match
        case st: Timer.SimpleTimer => renderSimpleTimer(st, boxSignal, signal, observer)
        case sct: Timer.StatusChangeTimer => renderStatusChangeTimer(sct, boxSignal, signal, observer)
    end renderTimer

    def renderSimpleTimer(timer: Timer.SimpleTimer, boxSignal: Signal[ChallengeBox], signal: Signal[Option[Timer]], observer: Observer[Boolean]): Element =
      div(
        className := "timer-box",
        div(
          className := "shown-checkboxes",
          (1 to timer.total).map(n => renderTimerCheckbox(n, signal, observer)),
        ),
        div(
          className := "obfuscated-checkboxes",
          span(
            input(
              tpe := "checkbox",
              className := "challenge-box-obfuscated",
              onClick --> { _ => observer.onNext(true) }
            )
          )
        )
      )
    end renderSimpleTimer

    def renderTimerCheckbox(n: Int,
                            signal: Signal[Option[Timer]],
                            observer: Observer[Boolean]): Element =
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

    def renderStatusChangeTimer(timer: Timer.StatusChangeTimer,
                                boxSignal: Signal[ChallengeBox],
                                signal: Signal[Option[Timer]],
                                observer: Observer[Boolean]): Element =
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
      val name: Var[String] = Var("")
      val challengeCheckboxes: Var[Int] = Var(1)
      val timerCheckboxes: Var[Int] = Var(0)

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
            onInput.mapToValue --> { n => name.update { _ => n } },
          )
        ),
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
          onClick.compose(_.withCurrentValueOf(nameSignal, challengeCheckboxesSignal, timerCheckboxesSignal)) --> { case (_, n, c, t) =>
            model.createSimpleChallenge(n, c, Some(t).filter(_ >= 1))
          }
        )
      )
    end renderChallengeCreator
end ChallengeCreator

final class ChallengeCreatorModel:
    val challenges: Var[List[ChallengeBox]] = Var(List())
    val challengesSignal = challenges.signal

    def createSimpleChallenge(name: String, challengeChecks: Int, timerChecks: Option[Int]): Unit =
      challenges.update { cs =>
        val c = ChallengeBox.createSimpleChallengeBox(name, challengeChecks)
        cs :+  timerChecks.fold(c)(n => c.addTimer(Timer.createSimpleTimer(n)))
      }
    end createSimpleChallenge

    def observerForIds[A](boxId: ChallengeBoxId,
                          challengeId: SimpleChallengeId,
                          f: (SimpleChallenge, A) => Option[SimpleChallenge]): Observer[A] =
      challenges.updater { case (boxes, a) =>
        boxes.map { box =>
          if(box.id == boxId) {
            box.updateAtId(challengeId, challenge => f(challenge, a))
          } else {
            Some(box)
          }
        }.collect { case Some(box) => box }
      }
    end observerForIds

    def checkboxObserver(boxId: ChallengeBoxId, challengeId: SimpleChallengeId): Observer[(Boolean, Int)] =
      observerForIds(boxId, challengeId, (c, checkedAt) => Some(c.checkAtBox(checkedAt._1, checkedAt._2)))
    end checkboxObserver

    def escalationObserver(boxId: ChallengeBoxId, challengeId: SimpleChallengeId): Observer[Unit] =
      observerForIds(boxId, challengeId, (c, _) => Some(c.toggleEscalate()))
    end escalationObserver

    def timerObserver(timerId: TimerId): Observer[Boolean] =
      challenges.updater { case (boxes, checked) =>
        boxes.map { box =>
          box.updateTimer(timerId, timer => Some(if checked then timer.checkBox() else timer.uncheckBox()))
        }
      }
    end timerObserver

    def nameObserver(boxId: ChallengeBoxId): Observer[String] =
      challenges.updater { (boxes, name) =>
        boxes.map { box =>
          if(box.id == boxId) {
            box.copy(name = name)
          } else {
            box
          }
        }
      }
    end nameObserver
end ChallengeCreatorModel
