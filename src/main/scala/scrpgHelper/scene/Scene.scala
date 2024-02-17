package scrpgHelper.scene

import scrpgHelper.status.Status

case class Scene[A](green: Int, yellow: Int, red: Int, position: Int, actorQueue: ActorQueue[A]):
    def totalSpaces: Int = green + yellow + red

    def resetActors: Scene[A] = this.copy(actorQueue = actorQueue.reset)
    def resetPosition: Scene[A] = this.copy(position = 1)

    def reset: Scene[A] = resetActors.resetPosition

    def legalPosition: Boolean =
      position <= totalSpaces && position >= 1 && green >= 0 && yellow >= 0 && red >= 0
    end legalPosition

    def updateGreen(newGreen: Int): Option[Scene[A]] =
      Some(copy(green = newGreen)).filter(_.legalPosition)
    end updateGreen

    def updateYellow(newYellow: Int): Option[Scene[A]] =
      Some(copy(yellow = newYellow)).filter(_.legalPosition)
    end updateYellow

    def updateRed(newRed: Int): Option[Scene[A]] =
      Some(copy(red = newRed)).filter(_.legalPosition)
    end updateRed

    def advancePosition: Option[Scene[A]] =
      if(position < totalSpaces) Some(copy(position = position + 1)) else None
    end advancePosition

    def addActor(actor: A): Scene[A] =
      copy(actorQueue = actorQueue.addActor(actor))
    end addActor

    def removeActor(actor: A): Scene[A] =
      copy(actorQueue = actorQueue.removeActor(actor))
    end removeActor

    def updateActor(actor: A, f: A => Option[A]): Option[Scene[A]] =
      actorQueue.updateActor(actor, f).map(aq => copy(actorQueue = aq))
    end updateActor

    def statusBoxes: List[Status] =
      List.fill(green)(Status.Green) ++ List.fill(yellow)(Status.Yellow) ++ List.fill(red)(Status.Red)
    end statusBoxes

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
      resetActors.advancePosition
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
