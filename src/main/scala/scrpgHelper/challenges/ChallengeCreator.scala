package scrpgHelper.challenges

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

object ChallengeCreator:
    val model = new Model()

    def challengeCreator(): Element =
      div(
        h1("Challenge Creator"),
        renderChallengeCreator(),
        renderChallenges(),
      )
    end challengeCreator

    def renderChallenges(): Element =
      div(
        children <-- model.challengesSignal.split(_.id){ (id, cs, s) => renderChallenge(cs, s, model.varForId(id)) }
      )
    end renderChallenges

    def renderChallenge(box: ChallengeBox, signal: Signal[ChallengeBox], observer: Var[Option[ChallengeBox]]): Element =
      val owner: Owner = ???
      div(
        (box.challenge match
          case CompoundChallenge.Simple(c) => renderSimpleChallenge(c,
                                                                    signal.map(_.challenge.forId(c.id)),
                                                                    observer.zoom(mbox => mbox.flatMap(_.challenge.forId(c.id)))((box, mc) => {
                                                                                                                                   box.flatMap { b =>
                                                                                                                                     mc.flatMap { c =>
                                                                                                                                        b.updateAtId(c.id, _ => Some(c))
                                                                                                                                     }
                                                                                                                                   }
                                                                                                                   })(owner))
          case CompoundChallenge.And(cs) => renderSimultaneousChallenges(cs)
          case CompoundChallenge.AndThen(c, n) => renderSerialChallenges(c, n)
          case CompoundChallenge.Or(cs) => renderBranchingChallenges(cs)),
        box.timers.map(renderTimer(_)),
      )
    end renderChallenge

    def renderSimpleChallenge(challenge: SimpleChallenge,
                              signal: Signal[Option[SimpleChallenge]],
                              observer: Var[Option[SimpleChallenge]]): Element =
      div(
        className := "challenge-box",
        (1 to challenge.total).map(n => renderChallengeCheckbox(n, signal, observer)),
      )
    end renderSimpleChallenge

    def renderChallengeCheckbox(n: Int,
                                signal: Signal[Option[SimpleChallenge]],
                                observer: Var[Option[SimpleChallenge]]): Element =
      span(
        className := "challenge-box-checkbox",
        input(
          tpe := "checkbox",
          checked <-- signal.map(_.fold(false)(c => c.checked <= n))
        )
      )
    end renderChallengeCheckbox

    def renderChallengeCheckbox(signal: Signal[Boolean], observer: Observer[Boolean]): Element =
      span(
        input(
          tpe := "checkbox",
          checked <-- signal,
          onChange.mapToChecked --> { b => observer.onNext(b) },
        )
      )
    end renderChallengeCheckbox

    def renderTimer(timer: Timer): Element =
      div(

      )
    end renderTimer

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

    def renderChallengeCreator(): Element =
      div(

      )
    end renderChallengeCreator
end ChallengeCreator

final class Model:
    val challenges: Var[List[ChallengeBox]] = Var(List(ChallengeBox.createSimpleChallengeBox(2)))
    val challengesSignal = challenges.signal

    def varForId(id: ChallengeBoxId): Var[Option[ChallengeBox]] =
      challenges.zoom(_.filter(_.id == id).headOption){ (boxes, mnewBox) =>
        mnewBox.fold(boxes.filterNot(_.id == id))(newBox => boxes.map(b => if b.id == id then newBox else b))
      }
    end varForId
end Model
