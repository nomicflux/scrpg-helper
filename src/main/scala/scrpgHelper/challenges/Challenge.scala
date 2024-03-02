package scrpgHelper.challenges

import scrpgHelper.status.Status

final class SimpleChallengeId

case class SimpleChallenge(
    id: SimpleChallengeId,
    name: Option[String],
    total: Int,
    checked: Int,
    escalated: Boolean
):
  def checkBox(): SimpleChallenge =
    if (checked < total) then copy(checked = checked + 1) else this
  end checkBox

  val completed: Boolean = checked >= total

  def setEscalate(e: Boolean): SimpleChallenge =
    copy(escalated = e)
  end setEscalate

  def toggleEscalate(): SimpleChallenge =
    copy(escalated = !escalated)
  end toggleEscalate

  def checkAtBox(toCheck: Boolean, n: Int): SimpleChallenge =
    if (toCheck && checked < total) {
      copy(checked = checked + 1)
    } else if (!toCheck && checked > 0) {
      copy(checked = checked - 1)
    } else {
      this
    }
  end checkAtBox
end SimpleChallenge

object SimpleChallenge:
  def createSimpleChallenge(name: Option[String], n: Int): SimpleChallenge =
    SimpleChallenge(new SimpleChallengeId(), name, n, 0, false)
  end createSimpleChallenge
end SimpleChallenge

enum CompoundChallenge:
  case Simple(challenge: SimpleChallenge)
  case And(challenges: List[CompoundChallenge])
  case AndThen(
      thisChallenge: CompoundChallenge,
      nextChallenge: CompoundChallenge
  )
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

  def getId(): Option[SimpleChallengeId] = this match
    case Simple(c) => Some(c.id)
    case _         => None
  end getId

  def completed(): Boolean = this match
    case Simple(challenge) => challenge.completed
    case And(challenges) =>
      challenges.foldLeft[Boolean](true)(_ && _.completed())
    case AndThen(thisChallenge, nextChallenge) =>
      thisChallenge.completed() && nextChallenge.completed()
    case Or(challenges) =>
      challenges.foldLeft[Boolean](false)(_ || _.completed())
  end completed

  def forId(id: SimpleChallengeId): Option[SimpleChallenge] = this match
    case Simple(challenge) => Some(challenge).filter(_.id == id)
    case And(challenges) =>
      challenges.foldLeft[Option[SimpleChallenge]](None)((acc, c) =>
        acc.orElse(c.forId(id))
      )
    case AndThen(thisChallenge, nextChallenge) =>
      thisChallenge.forId(id).orElse(nextChallenge.forId(id))
    case Or(challenges) =>
      challenges.foldLeft[Option[SimpleChallenge]](None)((acc, c) =>
        acc.orElse(c.forId(id))
      )
  end forId

  def updateAtId(
      id: SimpleChallengeId,
      f: SimpleChallenge => SimpleChallenge
  ): CompoundChallenge = this match
    case Simple(challenge) =>
      if challenge.id == id then Simple(f(challenge)) else this
    case And(challenges) => And(challenges.map(_.updateAtId(id, f)))
    case AndThen(thisChallenge, nextChallenge) =>
      thisChallenge.updateAtId(id, f).andThen(nextChallenge.updateAtId(id, f))
    case Or(challenges) => Or(challenges.map(_.updateAtId(id, f)))
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

  def andThen(
      challenge: CompoundChallenge,
      challenges: List[CompoundChallenge]
  ): CompoundChallenge =
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

  def toggleShown(): ChallengeBox =
    copy(shown = !shown)
  end toggleShown

  def updateAtId(
      id: SimpleChallengeId,
      f: SimpleChallenge => SimpleChallenge
  ): ChallengeBox =
    val newC = challenge.updateAtId(id, c => f(c))
    copy(challenge = newC)
  end updateAtId

  def updateTimer(id: TimerId, f: Timer => Timer): ChallengeBox =
    val newTimers = timers.map { timer =>
      if (timer.getId() == id) {
        f(timer)
      } else {
        timer
      }
    }
    copy(timers = newTimers)
  end updateTimer

  def addTimer(timer: Timer): ChallengeBox =
    copy(timers = timers :+ timer)
  end addTimer
end ChallengeBox

object ChallengeBox:
  def createSimpleChallengeBox(boxName: String, challengeName: Option[String], n: Int): ChallengeBox =
    ChallengeBox(
      new ChallengeBoxId(),
      boxName,
      CompoundChallenge.fromSimpleChallenge(
        SimpleChallenge.createSimpleChallenge(challengeName, n)
      ),
      List(),
      true
    )
  end createSimpleChallengeBox

  def createSimultaneousChallengeBox(
      boxName: String,
      ns: List[(Option[String], Int)]
  ): ChallengeBox =
    if (ns.size == 1) {
      createSimpleChallengeBox(boxName, ns.head._1, ns.head._2)
    } else {
      ChallengeBox(
        new ChallengeBoxId(),
        boxName,
        CompoundChallenge.and(
          ns.map(n =>
            CompoundChallenge.fromSimpleChallenge(
              SimpleChallenge.createSimpleChallenge(n._1, n._2)
            )
          )
        ),
        List(),
        true
      )
    }
  end createSimultaneousChallengeBox
end ChallengeBox
