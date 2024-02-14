package scrpgHelper

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

@main
def SCRPGHelper(): Unit =
  renderOnDomContentLoaded(
    dom.document.getElementById("app"),
    Main.appElement()
  )

object Main:
    import Die.*
    import EffectDieType.*

    val model: Model = new Model

    val chartConfig =
      import typings.chartJs.mod.*
      new ChartConfiguration {
        `type` = ChartType.bar
        data = new ChartData {
          datasets = js.Array(
            new ChartDataSets {
              label = "Roll"
              borderWidth = 1
              backgroundColor = "purple"
            },
          )
        }
        options = new ChartOptions {
          scales = new ChartScales {
            yAxes = js.Array(new CommonAxe {
                               ticks = new TickOptions {
                                 beginAtZero = true
                               }
                             })
          }
        }
      }
    end chartConfig

    def appElement(): Element =
      div(
        h1("Rolls"),
        renderDice(),
        renderRollChart(),
      )
    end appElement

    def renderRollChart(): Element =
        import scala.scalajs.js.JSConverters.*
        import typings.chartJs.mod.*

        var optChart: Option[Chart] = None
        canvasTag(
          width := "100%",
          height := "200px",
          onMountUnmountCallback(
            mount = { nodeCtx =>
              val domCanvas: dom.HTMLCanvasElement = nodeCtx.thisNode.ref
              val chart = Chart.apply.newInstance2(domCanvas, chartConfig)
              optChart = Some(chart)
            },
            unmount = { thisNode =>
              optChart.foreach(_.destroy())
              optChart = None
            },
          ),
          model.currFreqs() --> { data =>
            val vals = 1 to data.keys.max
            val labels = vals.map(_.toString)
            val counts = vals.map(data.getOrElse(_, 0).toDouble)
            optChart.foreach { chart =>
              chart.data.labels = labels.toJSArray
              chart.data.datasets.get(0).data = counts.toJSArray
              chart.update()
            }
          }
        )
    end renderRollChart

    def renderDice(): Element =
      div(
        dieButtons(model.d1Signal, model.d1Updater()),
        dieButtons(model.d2Signal, model.d2Updater()),
        dieButtons(model.d3Signal, model.d3Updater()),
      )
    end renderDice

    def dieButtons(dieSignal: Signal[Die], dieObserver: Observer[Int]): Element =
      div(
        className := "dicegroup",
        dieButton(dieSignal, dieObserver, 4),
        dieButton(dieSignal, dieObserver, 6),
        dieButton(dieSignal, dieObserver, 8),
        dieButton(dieSignal, dieObserver, 10),
        dieButton(dieSignal, dieObserver, 12),
      )
    end dieButtons

    def dieButton(dieSignal: Signal[Die], dieObserver: Observer[Int], n: Int): Element =
      button(
        tpe := "button",
        className := s"die d$n",
        disabled <-- dieSignal.map(_.n == n),
        s"d$n",
        onClick --> { _event => dieObserver.onNext(n) }
      )
    end dieButton
end Main

final class Model:
    import Die.*
    import EffectDieType.*

    val d1Var: Var[Die] = Var(d(6))
    val d1Signal = d1Var.signal
    val d2Var: Var[Die] = Var(d(6))
    val d2Signal = d2Var.signal
    val d3Var: Var[Die] = Var(d(6))
    val d3Signal = d3Var.signal
    val eVar: Var[Seq[EffectDieType]] = Var(Seq(Mid))
    val eSignal = eVar.signal

    def currFreqs(): Signal[Map[Int, Int]] =
      eSignal.combineWith(d1Signal, d2Signal, d3Signal).map { (e, d1, d2, d3) =>
        freqs(d1, d2, d3, e)
      }
    end currFreqs

    def dieUpdater(dieVar: Var[Die]): Observer[Int] =
      dieVar.updater { (_die, n) => d(n) }
    end dieUpdater

    def d1Updater(): Observer[Int] = dieUpdater(d1Var)
    def d2Updater(): Observer[Int] = dieUpdater(d2Var)
    def d3Updater(): Observer[Int] = dieUpdater(d3Var)

    def updateEffectDieTypes(es: Seq[EffectDieType]) =
      eVar.update(_ => es)
end Model
