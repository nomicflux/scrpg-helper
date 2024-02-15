package scrpgHelper.scene

case class ActorQueue[A](acting: Option[A], acted: Set[A], remaining: Set[A]):
    def addActor(actor: A): ActorQueue[A] =
      copy(remaining = remaining + actor)
    end addActor

    def removeActor(actor: A): ActorQueue[A] =
      copy(acting.filterNot(_ == actor), acted.diff(Set(actor)), remaining.diff(Set(actor)))
    end removeActor

    def totalActors: Set[A] =
      acted.union(remaining).union(acting.toSet)
    end totalActors

    def reset: ActorQueue[A] =
      new ActorQueue(None, Set(), totalActors)
    end reset

    def shiftActor(actor: A): ActorQueue[A] =
      copy(Some(actor), acted.union(acting.toSet), remaining.diff(Set(actor)))
    end shiftActor

    def advanceQueue(actor: A): Option[ActorQueue[A]] =
      if(remaining.isEmpty) {
        None
      } else if(remaining.contains(actor)) {
        Some(shiftActor(actor))
      } else {
        Some(this)
      }
    end advanceQueue
end ActorQueue

object ActorQueue:
    def apply[A](): ActorQueue[A] =
      new ActorQueue(None, Set(), Set())
    end apply

    def apply[A](actors: Seq[A]): ActorQueue[A] =
      new ActorQueue(None, Set(), actors.toSet)
    end apply
end ActorQueue
