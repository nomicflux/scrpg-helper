package scrpgHelper.scene

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

object SceneTracker:
    import scrpgHelper.status.Status
    import scrpgHelper.components.NumBox

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
            renderActorList(q => q.acted.toSeq, true)
          ),
          td(
            className := "actors-current",
            renderActorList(q => q.acting.toSeq, false)
          ),
          td(
            className := "actors-remaining",
            renderActorList(q => q.remaining.toSeq, true)
          ),
        )),
        tfoot(
          tr(
            td(
              colSpan := 4,
              span(
                className := "help",
                "Click on Waiting actors to hand off the scene to them. Once all actors have gone, click on any actor ",
                "other than the current one to start the next round. Environment actors will automatically advance the tracker.",
              )
            )
          )
        )
      )
    end renderActors

    def renderActorList(getActors: ActorQueue[Actor] => Seq[Actor], withEmpty: Boolean): Element =
      ul(
        children <-- model.actorsSignal.map { q =>
          val actors = getActors(q)
          actors.map(renderActor) ++ (if withEmpty then renderEmpty(q.totalActors.size, actors.size) else List())
        }
      )
    end renderActorList

    def renderActor(actor: Actor): Element =
      li(
        div(
          className := "actor",
          span(
            className := actor.toClassName,
            actor.toString,
            onClick --> { _event => model.actorAdvancer.onNext(actor) }
          ),
          span(
            className := s"actor-change-die-size actor-has-die-${actor.actorType.hasDie}",
            button(
              tpe := "button",
              disabled := actor.actorType.getDieSize.filter(_ == 12).isDefined,
              "➚",
              onClick --> { _ => model.actorDieIncreaser.onNext(actor) },
            ),
            button(
              tpe := "button",
              disabled := actor.actorType.getDieSize.filter(_ == 4).isDefined,
              "➘",
              onClick --> { _ => model.actorDieDecreaser.onNext(actor) },
            )
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

      val actorType: Var[ActorType] = Var(ActorType.Hero)
      val actorTypeSignal = actorType.signal

      val actorAlreadyActed: Var[Boolean] = Var(false)
      val actorAlreadyActedSignal = actorAlreadyActed.signal

      val dieSize: Var[Int] = Var(4)
      val dieSizeSignal = dieSize.signal

      div(
        className := "add-actor",
        span(
          input(
            tpe := "text",
            value <-- actorName,
            onInput.mapToValue --> actorName,
            onKeyPress.compose(_.withCurrentValueOf(actorTypeSignal.combineWith(actorAlreadyActedSignal))) --> { case (event, at, acted) =>
              if(event.key == "Enter") {
                actorName.update { name =>
                  if(acted) then model.actorActedAdder.onNext(Actor.createActor(at, name)) else model.actorAdder.onNext(Actor.createActor(at, name))
                  ""
                }
              }
            },
          )
        ),
        span(
          select(
            option(value := "Hero", "Hero"),
            option(value := "Villain", "Villain"),
            option(value := "Minion", "Minion"),
            option(value := "Lieutenant", "Lieutenant"),
            option(value := "Environment", "Environment"),
            option(value := "Other", "Other"),
            onChange.mapToValue.compose(_.withCurrentValueOf(dieSizeSignal)) --> { case(s, d) =>
              ActorType.fromString(s, d).foreach(at => actorType.update(_ => at))
            }
          ),
          select(
            className <-- actorTypeSignal.map(at => s"select-die-size actor-has-die-${at.hasDie}"),
            option(value := "4", "d4"),
            option(value := "6", "d6"),
            option(value := "8", "d8"),
            option(value := "10", "d10"),
            option(value := "12", "d12"),
            onChange.mapToValue.map(_.toIntOption).collect { case Some(n) => n } --> { n =>
              dieSize.update(_ => n)
              actorType.update(at => at.withDieSize(n))
            },
          )
        ),
        span(
          "Start as acted",
          input(
            tpe := "checkbox",
            checked <-- actorAlreadyActedSignal,
            onChange.mapToValue --> { _ => actorAlreadyActed.update(b => !b) }
          )
        ),
        span(
          button(
            tpe := "button",
            "Add Actor",
            onClick.compose(_.withCurrentValueOf(actorTypeSignal.combineWith(actorAlreadyActedSignal))) --> { case (_event, at, acted) =>
              actorName.update { name =>
                if(acted) then model.actorActedAdder.onNext(Actor.createActor(at, name)) else model.actorAdder.onNext(Actor.createActor(at, name))
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
        NumBox(model.greenSignal, model.greenIncrementer, model.greenUpdater)
          .withLabel("Green").withSpanClassName("status-updater-span").withInputClassName("status-updater status-green")
          .withMinVal(0)
          .render(),
        NumBox(model.yellowSignal, model.yellowIncrementer, model.yellowUpdater)
          .withLabel("Yellow").withSpanClassName("status-updater-span").withInputClassName("status-updater status-yellow")
          .withMinVal(0)
          .render(),
        NumBox(model.redSignal, model.redIncrementer, model.redUpdater)
          .withLabel("Red").withSpanClassName("status-updater-span").withInputClassName("status-updater status-red")
          .withMinVal(0)
          .render(),
      )
    end renderBoxUpdater
end SceneTracker

final class Model:
    val sceneTracker: Var[Scene[Actor]] = Var(Scene(2,4,2))
    val sceneSignal = sceneTracker.signal

    val greenSignal = sceneSignal.map(_.green)
    val yellowSignal = sceneSignal.map(_.yellow)
    val redSignal = sceneSignal.map(_.red)

    val prevScenesVar: Var[List[Scene[Actor]]] = Var(List())
    val numOfPrevScenes: Signal[Int] = prevScenesVar.signal.map(_.size)

    val redoScenesVar: Var[List[Scene[Actor]]] = Var(List())
    val numOfRedoScenes: Signal[Int] = redoScenesVar.signal.map(_.size)

    val actorsSignal = sceneSignal.map(_.actorQueue)

    def savePrevScene(scene: Scene[Actor]): Unit =
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

    def optSceneUpdater[A](f: (Scene[Actor], A) => Option[Scene[Actor]]): Observer[A] =
      sceneTracker.updater { (scene, a) =>
        f(scene, a).fold(scene){ s =>
          if s != scene then savePrevScene(scene) else ()
          s
        }
      }
    end optSceneUpdater

    def sceneUpdater[A](f: (Scene[Actor], A) => Scene[Actor]): Observer[A] =
      sceneTracker.updater { (scene, a) =>
        val s = f(scene, a)
        if s != scene then savePrevScene(scene) else ()
        s
      }
    end sceneUpdater

    val advanceTracker: Observer[Unit] = optSceneUpdater((scene, _) => scene.advancePosition)
    val resetScene: Observer[Unit] = sceneUpdater((scene, _) => scene.reset)

    val actorAdder: Observer[Actor] = sceneUpdater((scene, actor) => scene.addActor(actor))
    val actorActedAdder: Observer[Actor] = sceneUpdater((scene, actor) => scene.addActedActor(actor))
    val actorRemover: Observer[Actor] = sceneUpdater((scene, actor) => scene.removeActor(actor))

    val greenUpdater: Observer[Int] = optSceneUpdater((scene, green) => scene.updateGreen(green))
    val yellowUpdater: Observer[Int] = optSceneUpdater((scene, yellow) => scene.updateYellow(yellow))
    val redUpdater: Observer[Int] = optSceneUpdater((scene, red) => scene.updateRed(red))

    val greenIncrementer: Observer[Int] = optSceneUpdater((scene, change) => scene.updateGreen(scene.green + change))
    val yellowIncrementer: Observer[Int] = optSceneUpdater((scene, change) => scene.updateYellow(scene.yellow + change))
    val redIncrementer: Observer[Int] = optSceneUpdater((scene, change) => scene.updateRed(scene.red + change))

    val actorAdvancer: Observer[Actor] = optSceneUpdater {  (scene, actor) =>
      val advanced = scene.advanceScene(actor)
      if actor.actorType.advancesScene() then advanced.flatMap(_.advancePosition) else advanced
    }
    val actorDieIncreaser: Observer[Actor] = optSceneUpdater((scene, actor) => scene.updateActor(actor, a => a.increaseDieSize()))
    val actorDieDecreaser: Observer[Actor] = optSceneUpdater((scene, actor) => scene.updateActor(actor, a => a.decreaseDieSize()))
end Model
