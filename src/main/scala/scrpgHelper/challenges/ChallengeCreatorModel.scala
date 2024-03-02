package scrpgHelper.challenges

import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom

import scala.scalajs.js
import scala.scalajs.js.annotation.*

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
