package scrpgHelper.scene

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

object SceneTracker:
    import scrpgHelper.status.Status

    val model = new Model

    def sceneTracker(): Element =
      div(
        className <-- model.sceneSignal.map { scene =>
          s"scene-tracker-page scene-status-${scene.currentStatus.toString.toLowerCase}"
        },
        h1("Scene Tracker"),
        advanceTrackerButton(),
        div(undoButton(), redoButton()),
        renderSceneTracker(),
        renderActors(),
        addActorInput(),
        renderBoxUpdater(),
        resetButton(),
      )
    end sceneTracker

    def resetButton(): Element =
      div(
        className := "reset",
        button(
          tpe := "button",
          className := "reset-button",
          "Reset",
          onClick --> { _ => model.resetScene() }
        )
      )
    end resetButton

    def undoButton(): Element =
      button(
        tpe := "button",
        className := "undo",
        "Undo",
        disabled <-- model.numOfPrevScenes.map(_ == 0),
        onClick --> { _ => model.undo() }
      )
    end undoButton

    def redoButton(): Element =
      button(
        tpe := "button",
        className := "redo",
        "Redo",
        disabled <-- model.numOfRedoScenes.map(_ == 0),
        onClick --> { _ => model.redo() }
      )
    end redoButton

    def renderSceneTracker(): Element =
      table(
        tr(
          className := "scene-tracker",
          children <-- model.sceneSignal.map { scene =>
            val statusBoxes = scene.statusBoxes
            statusBoxes.zip(statusBoxes.indices).map( (status, position) => renderSceneBox(status, position + 1, scene.position))
          }
        )
      )
    end renderSceneTracker

    def advanceTrackerButton(): Element =
      button(
        tpe := "button",
        className := "advance-tracker",
        "Advance Tracker",
        onClick --> { _event => model.advanceTracker() }
      )
    end advanceTrackerButton

    def renderActors(): Element =
      table(
        className := "actors",
        tr(th("Acted"), th("Acting"), th("Waiting")),
        tr(
          td(
            className := "actors-acted",
            ul(
              children <-- model.actorsSignal.map(_.acted.map(renderActor(_)).toSeq)
            )
          ),
          td(
            className := "actors-current",
            ul(
              children <-- model.actorsSignal.map(_.acting.map(renderActor(_)).toSeq)
            )
          ),
          td(
            className := "actors-remaining",
            ul(
              children <-- model.actorsSignal.map(_.remaining.map(renderActor(_)).toSeq)
            )
          )
        ),
      )
    end renderActors

    def renderActor(actor: String): Element =
      li(
        div(
          className := "actor",
          span(
            className := "actor-name",
            actor,
            onClick --> { _event => model.actorAdvancer.onNext(actor) }
          ),
          span(
            className := "actor-remove",
            "✗",
            onClick --> { _event => model.actorRemover.onNext(actor) }
          )
        )
      )
    end renderActor

    def renderSceneBox(status: Status, position: Int, currentPosition: Int): Element =
      val statusClass = s"scene-${status.toString.toLowerCase}"
      val positionClass = if(position == currentPosition) {
        "scene-position"
      } else if(position < currentPosition) {
        "scene-done"
      } else {
        "scene-future"
      }
      val positionText = if position == currentPosition then "✘" else " "
      td(
          className := s"scene-box $statusClass $positionClass",
          div(
            span(positionText),
          ),
      )
    end renderSceneBox

    def addActorInput(): Element =
      val actorName: Var[String] = Var("")
      val actorSignal = actorName.signal

      div(
        className := "add-actor",
        input(
          `typ` := "text",
          value <-- actorName,
          onInput.mapToValue --> actorName
        ),
        button(
          tpe := "button",
          "Add Actor",
          onClick --> { _event =>
            actorName.update { name =>
              model.actorUpdater.onNext(name)
              ""
            }
          }
        )
      )
    end addActorInput

    def renderBoxUpdater(): Element =
      div(
        numInput(model.greenSignal, model.greenUpdater, "Green"),
        numInput(model.yellowSignal, model.yellowUpdater, "Yellow"),
        numInput(model.redSignal, model.redUpdater, "Red"),
      )
    end renderBoxUpdater

    def numInput(nSignal: Signal[Int], nObserver: Observer[Int], label: String): Element =
        span(
          className := s"status-updater-span",
          span(label),
          input(
            `typ` := "number",
            size := 2,
            className := s"status-updater status-${label.toLowerCase}",
            controlled(
              value <-- nSignal.map(_.toString),
              onInput.mapToValue.map(_.toIntOption).map(_.filter(_ >= 0)).collect {
                case Some(n)  => n
              } --> nObserver
            )
          )
        )
    end numInput
end SceneTracker

final class Model:
    val sceneTracker: Var[Scene[String]] = Var(Scene(2,4,2))
    val sceneSignal = sceneTracker.signal

    val greenSignal = sceneSignal.map(_.green)
    val yellowSignal = sceneSignal.map(_.yellow)
    val redSignal = sceneSignal.map(_.red)

    val prevScenesVar: Var[List[Scene[String]]] = Var(List())
    val numOfPrevScenes: Signal[Int] = prevScenesVar.signal.map(_.size)

    val redoScenesVar: Var[List[Scene[String]]] = Var(List())
    val numOfRedoScenes: Signal[Int] = redoScenesVar.signal.map(_.size)

    val actorsSignal = sceneSignal.map(_.actorQueue)

    def savePrevScene(scene: Scene[String]): Unit =
      prevScenesVar.update { scenes => scene +: scenes }
      redoScenesVar.update { _ => List() }
    end savePrevScene

    def undo(): Unit =
      prevScenesVar.update { prevScenes =>
        prevScenes.headOption.foreach { newScene =>
          sceneTracker.update { oldScene =>
            redoScenesVar.update(redoScenes => oldScene +: redoScenes)
            newScene
          }
        }
        prevScenes.tail
      }
    end undo

    def redo(): Unit =
      redoScenesVar.update { redoScenes =>
        redoScenes.headOption.foreach { oldScene =>
          sceneTracker.update { currScene =>
            prevScenesVar.update(prevScenes => currScene +: prevScenes)
            oldScene
          }
        }
        redoScenes.tail
      }
    end redo

    def advanceTracker(): Unit =
      sceneTracker.update { scene =>
        scene.advancePosition.fold(scene){ s =>
          if s != scene then savePrevScene(scene) else ()
          s
        }
      }
    end advanceTracker

    def resetScene(): Unit =
      sceneTracker.update { scene =>
        val s = scene.reset
        if s != scene then savePrevScene(scene) else ()
        s
      }
    end resetScene

    val actorUpdater: Observer[String] =
      sceneTracker.updater { (scene, actor) =>
        val s = scene.addActor(actor)
        if s != scene then savePrevScene(scene) else ()
        s
      }

    val actorRemover: Observer[String] =
      sceneTracker.updater { (scene, actor) =>
        val s = scene.removeActor(actor)
        if s != scene then savePrevScene(scene) else ()
        s
      }

    val greenUpdater: Observer[Int] =
      sceneTracker.updater { (scene, green) =>
        scene.updateGreen(green).fold(scene){ s =>
          if s != scene then savePrevScene(scene) else ()
          s
        }
      }

    val yellowUpdater: Observer[Int] =
      sceneTracker.updater { (scene, yellow) =>
        scene.updateYellow(yellow).fold(scene){ s =>
          if s != scene then savePrevScene(scene) else ()
          s
        }
      }

    val redUpdater: Observer[Int] =
      sceneTracker.updater { (scene, red) =>
        scene.updateRed(red).fold(scene){ s =>
          if s != scene then savePrevScene(scene) else ()
          s
        }
      }

    val actorAdvancer: Observer[String] =
      sceneTracker.updater { (scene, actor) =>
        scene.advanceScene(actor).fold(scene){ s =>
          if s != scene then savePrevScene(scene) else ()
          s
        }
      }
end Model
