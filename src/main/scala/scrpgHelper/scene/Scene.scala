package scrpgHelper.scene

import scrpgHelper.status.Status

case class Scene[A](green: Int, yellow: Int, red: Int, position: Int, actorQueue: ActorQueue[A]):
    def totalSpaces: Int = green + yellow + red

    def reset: Scene[A] = this.copy(position = position + 1, actorQueue = actorQueue.reset)

    def currentStatus: Status =
      if (position <= green) {
        Status.Green
      } else if (position <= (green + yellow)) {
        Status.Yellow
      } else if (position <= totalSpaces) {
        Status.Red
      } else {
        Status.Out
      }
    end currentStatus

    def advanceTracker: Option[Scene[A]] =
      if (position == totalSpaces) None else Some(reset)
    end advanceTracker

    def advanceScene(actor: A): Option[Scene[A]] =
      actorQueue.advanceQueue(actor).map(q => copy(actorQueue = q)).orElse(advanceTracker.flatMap(_.advanceScene(actor)))
    end advanceScene
end Scene

object Scene:
    def apply[A](green: Int, yellow: Int, red: Int): Scene[A] =
      new Scene(green, yellow, red, 1, ActorQueue())
    end apply

    def apply[A](green: Int, yellow: Int, red: Int, actors: List[A]): Scene[A] =
      new Scene(green, yellow, red, 1, ActorQueue(actors))
    end apply
end Scene
