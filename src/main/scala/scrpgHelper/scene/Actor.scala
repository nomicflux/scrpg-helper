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

enum Actor:
    case Hero(id: ActorId, name: String)
    case Villain(id: ActorId, name: String)
    case Environment(id: ActorId, name: String)
    case Other(id: ActorId, name: String)
    case Minion(id: ActorId, name: String, dieSize: Int)
    case Lieutenant(id: ActorId, name: String, dieSize: Int)

    val toClassName: String = this match
        case Hero(_, _) => "actor-name actor-hero"
        case Villain(_, _) => "actor-name actor-villain"
        case Environment(_, _) => "actor-name actor-environment"
        case Other(_, _) => "actor-name actor-other"
        case Minion(_, _, dieSize) => s"actor-name actor-minion actor-die-size-${dieSize}"
        case Lieutenant(_, _, dieSize) => s"actor-name actor-lieutenant actor-die-size-${dieSize}"
    end toClassName

    override val toString: String = this match
        case Hero(_, name) => s"(H) $name"
        case Villain(_, name) => s"(V) $name"
        case Environment(_, name) => s"(E) $name"
        case Other(_, name) => name
        case Minion(_, name, dieSize) => s"(M) $name (d$dieSize)"
        case Lieutenant(_, name, dieSize) => s"(L) $name (d$dieSize)"
    end toString
end Actor

object Actor:
    def createActor(actorType: ActorType, name: String): Actor = actorType match
        case ActorType.Hero => new Hero(new ActorId, name)
        case ActorType.Villain => new Villain(new ActorId, name)
        case ActorType.Environment => new Environment(new ActorId, name)
        case ActorType.Other => new Other(new ActorId, name)
        case ActorType.Minion(dieSize) => new Minion(new ActorId, name, dieSize)
        case ActorType.Lieutenant(dieSize) => new Lieutenant(new ActorId, name, dieSize)
    end createActor
end Actor
