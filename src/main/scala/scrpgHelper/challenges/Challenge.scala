package scrpgHelper.challenges

import scrpgHelper.status.Status
import typings.std.stdStrings.offer

final class SimpleChallengeId

case class SimpleChallenge(id: SimpleChallengeId, total: Int, checked: Int, escalated: Boolean):
    def checkBox(): SimpleChallenge =
      if(checked < total) then copy(checked = checked + 1) else this
    end checkBox

    val completed: Boolean = checked >= total

    def setEscalate(e: Boolean): SimpleChallenge =
      copy(escalated = e)
    end setEscalate

    def checkAtBox(toCheck: Boolean, n: Int): SimpleChallenge =
      if(toCheck) {
        copy(checked = checked + 1)
      } else {
        copy(checked = checked - 1)
      }
    end checkAtBox
end SimpleChallenge

object SimpleChallenge:
    def createSimpleChallenge(n: Int): SimpleChallenge =
        SimpleChallenge(new SimpleChallengeId(), n, 0, false)
    end createSimpleChallenge
end SimpleChallenge

final class TimerId

enum Timer:
    case SimpleTimer(id: TimerId, total: Int, checked: Int)
    case StatusChangeTimer(id: TimerId, onStatus: Set[Status])

    def getId(): TimerId = this match
        case SimpleTimer(id, _, _) => id
        case StatusChangeTimer(id, _) => id
    end getId

    def checkBox(): Timer = this match
        case SimpleTimer(id, total, checked) => if(checked < total) then SimpleTimer(id, total, checked + 1) else this
        case StatusChangeTimer(_, _) => this
    end checkBox

    def uncheckBox(): Timer = this match
        case SimpleTimer(id, total, checked) => if(checked > 0) then SimpleTimer(id, total, checked - 1) else this
        case StatusChangeTimer(_, _) => this
    end uncheckBox

    def getChecked(): Int = this match
        case SimpleTimer(_, _, checked) => checked
        case StatusChangeTimer(_, _) => 0
    end getChecked

    def completed(currentStatus: Option[Status]): Boolean = this match
        case SimpleTimer(_, total, checked) => checked >= total
        case StatusChangeTimer(_, statuses) => currentStatus.fold(false)(s => !statuses.filter(s >= _).isEmpty)
    end completed
end Timer

object Timer:
    def createSimpleTimer(n: Int): Timer =
      SimpleTimer(new TimerId(), n, 0)
    end createSimpleTimer

    def createYellowStatusTimer(): Timer =
      StatusChangeTimer(new TimerId(), Set(Status.Yellow))
    end createYellowStatusTimer

    def createRedStatusTimer(): Timer =
      StatusChangeTimer(new TimerId(), Set(Status.Red))
    end createRedStatusTimer

    def createStatusChangeTimer(currentStatus: Status): Timer =
      StatusChangeTimer(new TimerId(), Set(Status.Yellow, Status.Red).filterNot(_ == currentStatus))
    end createStatusChangeTimer
end Timer

enum CompoundChallenge:
    case Simple(challenge: SimpleChallenge)
    case And(challenges: List[CompoundChallenge])
    case AndThen(thisChallenge: CompoundChallenge, nextChallenge: CompoundChallenge)
    case Or(challenges: List[CompoundChallenge])

    def andThen(challenge: CompoundChallenge): CompoundChallenge =
      AndThen(this, challenge)
    end andThen

    def andThen(challenges: List[CompoundChallenge]): CompoundChallenge =
      challenges.fold(this)(AndThen(_, _))
    end andThen

    def and(challenge: CompoundChallenge): CompoundChallenge =
      And(List(this, challenge))
    end and

    def and(challenges: List[CompoundChallenge]): CompoundChallenge =
      And(this +: challenges)
    end and

    def or(challenge: CompoundChallenge): CompoundChallenge =
      Or(List(this, challenge))
    end or

    def or(challenges: List[CompoundChallenge]): CompoundChallenge =
      Or(this +: challenges)
    end or

    def completed(): Boolean = this match
      case Simple(challenge) => challenge.completed
      case And(challenges) => challenges.foldLeft[Boolean](true)(_ && _.completed())
      case AndThen(thisChallenge, nextChallenge) => thisChallenge.completed() && nextChallenge.completed()
      case Or(challenges) => challenges.foldLeft[Boolean](false)(_ || _.completed())
    end completed

    def forId(id: SimpleChallengeId): Option[SimpleChallenge] = this match
      case Simple(challenge) => Some(challenge).filter(_.id == id)
      case And(challenges) => challenges.foldLeft[Option[SimpleChallenge]](None)((acc, c) => acc.orElse(c.forId(id)))
      case AndThen(thisChallenge, nextChallenge) => thisChallenge.forId(id).orElse(nextChallenge.forId(id))
      case Or(challenges) => challenges.foldLeft[Option[SimpleChallenge]](None)((acc, c) => acc.orElse(c.forId(id)))
    end forId

    def updateAtId(id: SimpleChallengeId, f: SimpleChallenge => Option[SimpleChallenge]): Option[CompoundChallenge] = this match
      case Simple(challenge) => Some(challenge).filter(_.id == id).flatMap(f).map(Simple(_))
      case And(challenges) => Some(And(challenges.collect(c => c.updateAtId(id, f) match
                                                       case Some(v) => v)))
      case AndThen(thisChallenge, nextChallenge) => thisChallenge.updateAtId(id, f).orElse(nextChallenge.updateAtId(id, f))
      case Or(challenges) => Some(Or(challenges.collect(c => c.updateAtId(id, f) match
                                                       case Some(v) => v)))
    end updateAtId
end CompoundChallenge

object CompoundChallenge:
    def fromSimpleChallenge(challenge: SimpleChallenge) =
      Simple(challenge)
    end fromSimpleChallenge

    def and(challenges: List[CompoundChallenge]): CompoundChallenge =
      And(challenges)
    end and

    def or(challenges: List[CompoundChallenge]): CompoundChallenge =
      Or(challenges)
    end or

    def andThen(challenge: CompoundChallenge, challenges: List[CompoundChallenge]): CompoundChallenge =
      challenges.fold(challenge)(_.andThen(_))
    end andThen
end CompoundChallenge

final class ChallengeBoxId

case class ChallengeBox(
  id: ChallengeBoxId,
  name: String,
  challenge: CompoundChallenge,
  timers: List[Timer],
  shown: Boolean
):
    def completed(): Boolean =
      challenge.completed()
    end completed

    def timeout(status: Option[Status]): Boolean =
      timers.foldLeft[Boolean](false)(_ || _.completed(status))
    end timeout

    def setShown(s: Boolean): ChallengeBox =
      copy(shown = s)
    end setShown

    def updateAtId(id: SimpleChallengeId, f: SimpleChallenge => Option[SimpleChallenge]): Option[ChallengeBox] =
      val newC = challenge.updateAtId(id, c => f(c))
      newC.map(c => this.copy(challenge = c))
    end updateAtId

    def updateTimer(id: TimerId, f: Timer => Option[Timer]): ChallengeBox =
      val newTimers = timers.map { timer =>
        if(timer.getId() == id) {
          f(timer)
        } else {
          Some(timer)
        }
      }.collect { case Some(timer) => timer }
      copy(timers = newTimers)
    end updateTimer

    def addTimer(timer: Timer): ChallengeBox =
      copy(timers = timers :+ timer)
    end addTimer
end ChallengeBox

object ChallengeBox:
    def createSimpleChallengeBox(name: String, n: Int): ChallengeBox =
      ChallengeBox(
        new ChallengeBoxId(),
        name,
        CompoundChallenge.fromSimpleChallenge(SimpleChallenge.createSimpleChallenge(n)),
        List(),
        true
      )
    end createSimpleChallengeBox
end ChallengeBox
