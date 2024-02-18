package scrpgHelper.scene

final class ActorId

enum ActorType:
    case Hero, Villain, Environment, Other
    case Minion(dieSize: Int)
    case Lieutenant(dieSize: Int)

    val hasDie: Boolean = this match
        case Minion(_) => true
        case Lieutenant(_) => true
        case _ => false
    end hasDie

    def withDieSize(n: Int): ActorType = this match
        case Minion(_) => Minion(n)
        case Lieutenant(_) => Lieutenant(n)
        case _ => this
    end withDieSize

    def increaseDie(n: Int): Option[Int] =
      if(n == 4) {
        Some(6)
      } else if(n == 6) {
        Some(8)
      } else if(n == 8) {
        Some(10)
      } else if(n == 10) {
        Some(12)
      } else {
        None
      }
    end increaseDie

    def decreaseDie(n: Int): Option[Int] =
      if(n == 6) {
        Some(4)
      } else if(n == 8) {
        Some(6)
      } else if(n == 10) {
        Some(8)
      } else if(n == 12) {
        Some(10)
      } else {
        None
      }
    end decreaseDie

    def increaseDieSize(): Option[ActorType] = this match
        case Minion(n) => increaseDie(n).map(m => new Minion(m))
        case Lieutenant(n) => increaseDie(n).map(m => new Lieutenant(m))
        case _ => Some(this)
    end increaseDieSize

    def decreaseDieSize(): Option[ActorType] = this match
        case Minion(n) => decreaseDie(n).map(m => new Minion(m))
        case Lieutenant(n) => decreaseDie(n).map(m => new Lieutenant(m))
        case _ => Some(this)
    end decreaseDieSize

    def advancesScene(): Boolean = this match
      case Environment => true
      case Hero => false
      case Villain => false
      case Other => false
      case Minion(_) => false
      case Lieutenant(_) => false
    end advancesScene
end ActorType

object ActorType:
    def fromString(s: String, dieSize: Int): Option[ActorType] =
      if(s == "Hero") {
        Some(Hero)
      } else if(s == "Villain") {
        Some(Villain)
      } else if(s == "Environment") {
        Some(Environment)
      } else if(s == "Other") {
        Some(Other)
      } else if(s == "Minion") {
        Some(Minion(dieSize))
      } else if(s == "Lieutenant") {
        Some(Lieutenant(dieSize))
      } else {
        None
      }
    end fromString
end ActorType

case class Actor(id: ActorId, name: String, actorType: ActorType):
    val toClassName: String = actorType match
        case ActorType.Hero => "actor-name actor-hero"
        case ActorType.Villain => "actor-name actor-villain"
        case ActorType.Environment => "actor-name actor-environment"
        case ActorType.Other => "actor-name actor-other"
        case ActorType.Minion(dieSize) => s"actor-name actor-minion actor-die-size-${dieSize}"
        case ActorType.Lieutenant(dieSize) => s"actor-name actor-lieutenant actor-die-size-${dieSize}"
    end toClassName

    override val toString: String = actorType match
        case ActorType.Hero => s"(H) $name"
        case ActorType.Villain => s"(V) $name"
        case ActorType.Environment => s"(E) $name"
        case ActorType.Other => name
        case ActorType.Minion(dieSize) => s"(M) $name (d$dieSize)"
        case ActorType.Lieutenant(dieSize) => s"(L) $name (d$dieSize)"
    end toString

    def increaseDieSize(): Option[Actor] =
      actorType.increaseDieSize().map(at => copy(actorType = at))
    end increaseDieSize

    def decreaseDieSize(): Option[Actor] =
      actorType.decreaseDieSize().map(at => copy(actorType = at))
    end decreaseDieSize
end Actor

object Actor:
    def createActor(actorType: ActorType, name: String): Actor = new Actor(new ActorId, name, actorType)
end Actor
