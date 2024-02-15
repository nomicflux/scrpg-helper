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
        h1("Scene Tracker"),
        advanceTrackerButton(),
        div(undoButton(), redoButton()),
        renderActors(),
        renderSceneTracker(),
        addActorInput(),
      )
    end sceneTracker

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
      div(
        className := "actors",
        div(
          className := "actors-acted",
          h3("Acted"),
          ul(
            children <-- model.actorsSignal.map(_.acted.map(renderActor(_)).toSeq)
          )
        ),
        div(
          className := "actors-current",
          h3("Acting"),
          ul(
            children <-- model.actorsSignal.map(_.acting.map(renderActor(_)).toSeq)
          )
        ),
        div(
          className := "actors-remaining",
          h3("Waiting"),
          ul(
            children <-- model.actorsSignal.map(_.remaining.map(renderActor(_)).toSeq)
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
end SceneTracker

final class Model:
    val sceneTracker: Var[Scene[String]] = Var(Scene(2,4,2))
    val sceneSignal = sceneTracker.signal

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
        scene.advanceTracker.fold(scene){ s =>
          savePrevScene(scene)
          s
        }
      }
    end advanceTracker

    val actorUpdater: Observer[String] =
      sceneTracker.updater { (scene, actor) =>
        savePrevScene(scene)
        scene.addActor(actor)
      }

    val actorRemover: Observer[String] =
      sceneTracker.updater { (scene, actor) =>
        savePrevScene(scene)
        scene.removeActor(actor)
      }

    val actorAdvancer: Observer[String] =
      sceneTracker.updater { (scene, actor) =>
        scene.advanceScene(actor).fold(scene){ s =>
          if s != scene then savePrevScene(scene) else ()
          s
        }
      }
end Model
