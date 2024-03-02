package scrpgHelper.challenges

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.*

object RenderChallenge:
  import scrpgHelper.components.NameBox
  import scrpgHelper.status.Status

  def renderChallenges(model: ChallengeCreatorModel,
                       statusSignal: Signal[Option[Status]]): Element =
    div(
      className := "challenge-creator-section",
      children <-- model.challengesSignal.split(_.id) { (id, cs, s) =>
        renderChallengeBox(model, cs, s, statusSignal)
      }
    )
  end renderChallenges

  def renderChallengeBox(
      model: ChallengeCreatorModel,
      origBox: ChallengeBox,
      signal: Signal[ChallengeBox],
      statusSignal: Signal[Option[Status]]
  ): Element =
    val nameObserver = model.nameObserver(origBox.id)

    div(
      className := "challenge-box challenge-creator-holder",
      className <-- signal.map(box =>
        s"challenge-box-completed-${box.completed()}"
      ),
      className <-- signal.combineWith(statusSignal).map{ case (box, status) =>
        s"challenge-box-timeout-${box.timeout(status)}"
      },
      className <-- signal.map(box => s"challenge-box-shown-${box.shown}"),
      NameBox(origBox.name, signal.map(box => Some(box.name)), nameObserver).render(),
      renderChallenge(origBox, signal, model),
      origBox.timers.map(timer =>
        RenderTimer.renderTimer(
          timer,
          signal,
          signal.map(box =>
            box.timers.filter(_.getId() == timer.getId()).headOption
          ),
          statusSignal,
          model.timerCheckboxObserver(timer.getId()),
          model.timerNameObserver(timer.getId()),
        )
      ),
      div(
        className := "challenge-box-footer",
        div(
          className := "remove-challenge-box",
          "✘",
          onClick --> { _ =>
            model.challenges.update { cs => cs.filter(_.id != origBox.id) }
          }
        ),
        div(
          className := "show-challenge-box",
          span(
            "Show checkboxes on Scene Tracker:"
          ),
          span(
            className <-- signal.map(box =>
              s"shown-challenge-box-${box.shown}"
            ),
            "👁"
          ),
          onClick --> { _ =>
            model.challenges.update { cs =>
              cs.map { box =>
                if (box.id == origBox.id) {
                  box.toggleShown()
                } else { box }
              }
            }
          }
        )
      )
    )
  end renderChallengeBox

  def renderChallenge(
    origBox: ChallengeBox,
    signal: Signal[ChallengeBox],
    model: ChallengeCreatorModel,
  ): Element =
      origBox.challenge match
        case CompoundChallenge.Simple(c) =>
          renderSimpleChallenge(
            c,
            signal.map(_.challenge.forId(c.id)),
            model.escalationObserver(origBox.id, c.id),
            model.checkboxObserver(origBox.id, c.id),
            model.challengeNameObserver(origBox.id, c.id),
          )
        case CompoundChallenge.And(cs) =>
          renderSimultaneousChallenges(
            model,
            origBox.id,
            signal.map(b =>
              b.challenge match
                case CompoundChallenge.And(cs) => cs
                case _                         => List()
            ),
            signal.map(_.challenge)
          )
        case CompoundChallenge.AndThen(c, n) => renderSerialChallenges(c, n)
        case CompoundChallenge.Or(cs)        => renderBranchingChallenges(cs)
  end renderChallenge

  def renderSimpleChallenge(
      challenge: SimpleChallenge,
      signal: Signal[Option[SimpleChallenge]],
      escalationObserver: Observer[Unit],
      checkboxObserver: Observer[(Boolean, Int)],
      nameObserver: Observer[String]
  ): Element =
    div(
      className := "simple-challenge-box",
      className <-- signal.map(mc =>
        mc.fold("")(c => s"escalated-${c.escalated}")
      ),
      div(
        NameBox(challenge.name.getOrElse(""),
                signal.map(c => c.flatMap(_.name)),
                nameObserver).render(),
      ),
      div(
        className := "container-row",
        div(
          className := "checkbox-container escalation-checkbox-container",
          span(
            child.text <-- signal.map(mc =>
              mc.fold("")(c =>
                if c.escalated then "De-escalate" else "Escalate"
              )
            ),
            " Twists:"
          ),
          input(
            tpe := "checkbox",
            className := "escalate-checkbox",
            checked <-- signal.map(mc => mc.fold(false)(_.escalated))
          ),
          onClick --> { _ =>
            escalationObserver.onNext(())
          }
        ),
        div(
          className := "shown-checkboxes",
          (1 to challenge.total).map(n =>
            renderChallengeCheckbox(n, signal, checkboxObserver)
          )
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
        )
      )
    )
  end renderSimpleChallenge

  def renderChallengeCheckbox(
      n: Int,
      signal: Signal[Option[SimpleChallenge]],
      observer: Observer[(Boolean, Int)]
  ): Element =
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

  def renderSimultaneousChallenges(
      model: ChallengeCreatorModel,
      boxId: ChallengeBoxId,
      challenges: Signal[List[CompoundChallenge]],
      signal: Signal[CompoundChallenge]
  ): Element =
    div(
      children <-- challenges.map(_.collect {
        case CompoundChallenge.Simple(c) =>
          renderSimpleChallenge(
            c,
            signal.map(_.forId(c.id)),
            model.escalationObserver(boxId, c.id),
            model.checkboxObserver(boxId, c.id),
            model.challengeNameObserver(boxId, c.id),
          )
      })
    )
  end renderSimultaneousChallenges

  def renderSerialChallenges(
      challenge: CompoundChallenge,
      nextChallenge: CompoundChallenge
  ): Element =
    div(
    )
  end renderSerialChallenges

  def renderBranchingChallenges(challenges: List[CompoundChallenge]): Element =
    div(
    )
  end renderBranchingChallenges
end RenderChallenge
