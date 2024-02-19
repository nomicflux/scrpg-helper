package scrpgHelper.scene

case class ActorQueue[A](acting: Option[A], acted: List[A], remaining: List[A]):
    def addActor(actor: A): ActorQueue[A] =
      copy(remaining = remaining :+ actor)
    end addActor

    def removeActor(actor: A): ActorQueue[A] =
      copy(acting.filterNot(_ == actor), acted.filterNot(_ == actor), remaining.filterNot(_ == actor))
    end removeActor

    def replaceActor(actorA: A, actorB: A): ActorQueue[A] =
      copy(
        acting = acting.map(a => if a == actorA then actorB else a),
        acted = acted.map(a => if a == actorA then actorB else a),
        remaining = remaining.map(a => if a == actorA then actorB else a),
      )
    end replaceActor

    def updateActor(actor: A, f: A => Option[A]): Option[ActorQueue[A]] =
      f(actor).map(newActor => replaceActor(actor, newActor))
    end updateActor

    def totalActors: List[A] =
      acted ++ acting.toList ++ remaining
    end totalActors

    def reset: ActorQueue[A] =
      new ActorQueue(None, List(), totalActors.toList)
    end reset

    def shiftActor(actor: A): ActorQueue[A] =
      copy(Some(actor), acted ++ acting.toList, remaining.filterNot(_ == actor))
    end shiftActor

    def advanceQueue(actor: A): Option[ActorQueue[A]] =
      if(acting.contains(actor)) {
        Some(this)
      } else if(remaining.isEmpty) {
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
      new ActorQueue(None, List(), List())
    end apply

    def apply[A](actors: Seq[A]): ActorQueue[A] =
      new ActorQueue(None, List(), actors.toList)
    end apply
end ActorQueue
