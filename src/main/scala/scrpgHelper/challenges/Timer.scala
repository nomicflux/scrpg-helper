package scrpgHelper.challenges

import scrpgHelper.status.Status

final class TimerId

enum Timer:
  case SimpleTimer(id: TimerId, name: Option[String], total: Int, checked: Int)
  case StatusChangeTimer(id: TimerId, name: Option[String], onStatus: Set[Status])

  def getId(): TimerId = this match
    case SimpleTimer(id, _, _, _)    => id
    case StatusChangeTimer(id, _, _) => id
  end getId

  def getName(): Option[String] = this match
    case SimpleTimer(_, name, _, _) => name
    case StatusChangeTimer(_, name, _) => name
  end getName

  def changeName(name: String): Timer = this match
    case st: SimpleTimer => st.copy(name = Some(name))
    case sct: StatusChangeTimer => sct.copy(name = Some(name))
  end changeName

  def checkBox(): Timer = this match
    case SimpleTimer(id, name, total, checked) =>
      if (checked < total) then SimpleTimer(id, name, total, checked + 1) else this
    case StatusChangeTimer(_, _, _) => this
  end checkBox

  def uncheckBox(): Timer = this match
    case SimpleTimer(id, name, total, checked) =>
      if (checked > 0) then SimpleTimer(id, name, total, checked - 1) else this
    case StatusChangeTimer(_, _, _) => this
  end uncheckBox

  def getChecked(): Int = this match
    case SimpleTimer(_, _, _, checked) => checked
    case StatusChangeTimer(_, _, _)    => 0
  end getChecked

  def completed(currentStatus: Option[Status]): Boolean = this match
    case SimpleTimer(_, _, total, checked) => checked >= total
    case StatusChangeTimer(_, _, statuses) =>
      currentStatus.fold(false)(s => !statuses.filter(s >= _).isEmpty)
  end completed

  def statusString(): String = this match
    case SimpleTimer(_, _, _, _) => ""
    case StatusChangeTimer(_, _, statuses) => statuses.map(_.toString).reduceLeft(_ + "," + _)
  end statusString

end Timer

object Timer:
  def createSimpleTimer(name: Option[String], n: Int): Timer =
    SimpleTimer(new TimerId(), name, n, 0)
  end createSimpleTimer

  def createStatusTimer(name: Option[String], status: Status): Timer =
    StatusChangeTimer(new TimerId(), name, Set(status))
  end createStatusTimer

  def createTimer(name: Option[String], preTimer: PreTimer): Timer = preTimer match
    case PreTimer.WithNum(n) => createSimpleTimer(name, n)
    case PreTimer.WithStatus(s) => createStatusTimer(name, s)
  end createTimer

  def createStatusChangeTimer(name: Option[String], currentStatus: Status): Timer =
    StatusChangeTimer(
      new TimerId(),
      name,
      Set(Status.Yellow, Status.Red).filterNot(_ == currentStatus)
    )
  end createStatusChangeTimer
end Timer

enum PreTimer:
    case WithNum(n: Int)
    case WithStatus(status: Status)

    def toInt(): Option[Int] = this match
        case WithNum(n) => Some(n)
        case WithStatus(_) => None
    end toInt
end PreTimer

object PreTimer:
    def fromInt(n: Int): PreTimer =
      WithNum(n)
    end fromInt

    def toInt(p: PreTimer): Option[Int] =
      p.toInt()
    end toInt
end PreTimer
