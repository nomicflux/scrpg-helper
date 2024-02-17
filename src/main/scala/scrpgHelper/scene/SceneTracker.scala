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
        div(className := "undo-section", resetButton(), undoButton(), redoButton()),
        div(className := "setup-section", renderBoxUpdater()),
        div(advanceTrackerButton()),
        renderSceneTracker(),
        renderActors(),
      )
    end sceneTracker

    def resetButton(): Element =
      div(
        className := "reset",
        button(
          tpe := "button",
          className := "reset-button",
          "Reset",
          onClick --> { _ => model.resetScene.onNext(()) }
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
      div(
        className := "scene-tracker",
        table(
          tr(
            children <-- model.sceneSignal.map { scene =>
              val statusBoxes = scene.statusBoxes
              statusBoxes.zip(statusBoxes.indices).map( (status, position) => renderSceneBox(status, position + 1, scene.position))
            }
          )
        )
      )
    end renderSceneTracker

    def advanceTrackerButton(): Element =
      button(
        tpe := "button",
        className := "advance-tracker",
        "Advance Tracker",
        onClick --> { _event => model.advanceTracker.onNext(()) }
      )
    end advanceTrackerButton

    def renderActors(): Element =
      table(
        className := "actors",
        thead(tr(th(), th("Acted"), th("Acting"), th("Waiting"))),
        tbody(
        tr(
          td(
            addActorInput(),
          ),
          td(
            className := "actors-acted",
            ul(
              children <-- model.actorsSignal.map { q =>
                val acted = q.acted.toSeq
                acted.map(renderActor) ++ renderEmpty(q.totalActors.size, acted.size)
              }
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
              children <-- model.actorsSignal.map { q =>
                val remaining = q.remaining.toSeq
                remaining.map(renderActor) ++ renderEmpty(q.totalActors.size, remaining.size)
              }
            )
          ),
        )),
        tfoot(
          tr(
            td(
              colSpan := 4,
              span(
                className := "help",
                "Click on Waiting actors to hand off the scene to them. Once all actors have gone, click on any actor ",
                "other than the current one to advance the tracker and start the next round.",
              )
            )
          )
        )
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

    def renderEmpty(total: Int, used: Int): Seq[Element] =
      (0 until (total - used - 1)).map { _ =>
        li(
          div(
            className := "actor",
            span(
              className := "actor-empty",
              "",
            ),
          )
        )
      }
    end renderEmpty

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
        span(
          input(
            `typ` := "text",
            value <-- actorName,
            onInput.mapToValue --> actorName,
            onKeyPress --> { event =>
              if(event.key == "Enter") {
                actorName.update { name =>
                  model.actorUpdater.onNext(name)
                  ""
                }
              }
            },
          )
        ),
        span(
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
      )
    end addActorInput

    def renderBoxUpdater(): Element =
      div(
        className := "render-box-updater",
        numInput(model.greenSignal, model.greenIncrementer, model.greenUpdater, "Green"),
        numInput(model.yellowSignal,  model.yellowIncrementer, model.yellowUpdater, "Yellow"),
        numInput(model.redSignal, model.redIncrementer, model.redUpdater, "Red"),
      )
    end renderBoxUpdater

    def numInput(nSignal: Signal[Int], nIncrementer: Observer[Int], nObserver: Observer[Int], label: String): Element =
        span(
          className := s"status-updater-span",
          span(label),
          button(
            tpe := "button",
            className := "spinner",
            "-",
            onClick --> { _ =>  nIncrementer.onNext(-1) }
          ),
          input(
            tpe := "text",
            size := 1,
            className := s"status-updater status-${label.toLowerCase}",
            controlled(
              value <-- nSignal.map(_.toString),
              onInput.mapToValue.map(_.toIntOption).map(_.filter(_ >= 0)).collect {
                case Some(n)  => n
              } --> nObserver
            )
          ),
          button(
            tpe := "button",
            className := "spinner",
            "+",
            onClick --> { _ =>  model.greenIncrementer.onNext(1) }
          ),

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

    def optSceneUpdater[A](f: (Scene[String], A) => Option[Scene[String]]): Observer[A] =
      sceneTracker.updater { (scene, a) =>
        f(scene, a).fold(scene){ s =>
          if s != scene then savePrevScene(scene) else ()
          s
        }
      }
    end optSceneUpdater

    def sceneUpdater[A](f: (Scene[String], A) => Scene[String]): Observer[A] =
      sceneTracker.updater { (scene, a) =>
        val s = f(scene, a)
        if s != scene then savePrevScene(scene) else ()
        s
      }
    end sceneUpdater

    val advanceTracker: Observer[Unit] = optSceneUpdater((scene, _) => scene.advancePosition)
    val resetScene: Observer[Unit] = sceneUpdater((scene, _) => scene.reset)

    val actorUpdater: Observer[String] = sceneUpdater((scene, actor) => scene.addActor(actor))
    val actorRemover: Observer[String] = sceneUpdater((scene, actor) => scene.removeActor(actor))

    val greenUpdater: Observer[Int] = optSceneUpdater((scene, green) => scene.updateGreen(green))
    val yellowUpdater: Observer[Int] = optSceneUpdater((scene, yellow) => scene.updateYellow(yellow))
    val redUpdater: Observer[Int] = optSceneUpdater((scene, red) => scene.updateRed(red))

    val greenIncrementer: Observer[Int] = optSceneUpdater((scene, change) => scene.updateGreen(scene.green + change))
    val yellowIncrementer: Observer[Int] = optSceneUpdater((scene, change) => scene.updateYellow(scene.yellow + change))
    val redIncrementer: Observer[Int] = optSceneUpdater((scene, change) => scene.updateRed(scene.red + change))

    val actorAdvancer: Observer[String] = optSceneUpdater((scene, actor) => scene.advanceScene(actor))
end Model
