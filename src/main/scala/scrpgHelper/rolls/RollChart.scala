package scrpgHelper.rolls

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

object RollChart:
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
              label = "Count"
              borderWidth = 1
              backgroundColor = "#cccccc"
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

    def rollChart(): Element =
      div(
        h1("Roll Frequencies"),
        renderDice(),
        renderEffectPanel(),
        renderRollChart(),
      )
    end rollChart

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
        dieButtons(model.d1Signal, model.d1Updater(), "Power"),
        dieButtons(model.d2Signal, model.d2Updater(), "Quality"),
        dieButtons(model.d3Signal, model.d3Updater(), "Status"),
      )
    end renderDice

    def dieButtons(dieSignal: Signal[Die],
                   dieObserver: Observer[Int],
                   label: String): Element =
      div(
        className := "dicegroup",
        h3(label),
        dieButton(dieSignal, dieObserver, 4),
        dieButton(dieSignal, dieObserver, 6),
        dieButton(dieSignal, dieObserver, 8),
        dieButton(dieSignal, dieObserver, 10),
        dieButton(dieSignal, dieObserver, 12),
      )
    end dieButtons

    def dieButton(dieSignal: Signal[Die],
                  dieObserver: Observer[Int],
                  n: Int): Element =
      button(
        tpe := "button",
        className := s"die d$n",
        disabled <-- dieSignal.map(_.n == n),
        s"d$n",
        onClick --> { _event => dieObserver.onNext(n) }
      )
    end dieButton

    def renderEffectPanel(): Element =
      effectPanel(model.eSignal, model.effectDieTypeUpdater())
    end renderEffectPanel

    def effectPanel(effectSignal: Signal[Set[EffectDieType]],
                    effectObserver: Observer[EffectDieType]): Element =
      div(
        effectButton(effectSignal, effectObserver, Min),
        effectButton(effectSignal, effectObserver, Mid),
        effectButton(effectSignal, effectObserver, Max),
      )
    end effectPanel

    def effectButton(effectSignal: Signal[Set[EffectDieType]],
                     effectObserver: Observer[EffectDieType],
                     effect: EffectDieType): Element =
      button(
        tpe := "button",
        className <-- effectSignal.map { effects =>
          val baseClass = s"effect-die selected ${effect.toString.toLowerCase}"
          if effects.contains(effect)
          then s"$baseClass selected"
          else s"$baseClass unselected"
        },
        s"${effect.toString}",
        onClick --> { _event => effectObserver.onNext(effect) }
      )
    end effectButton
end RollChart

final class Model:
    import Die.*
    import EffectDieType.*

    val d1Var: Var[Die] = Var(d(6))
    val d1Signal = d1Var.signal
    val d2Var: Var[Die] = Var(d(6))
    val d2Signal = d2Var.signal
    val d3Var: Var[Die] = Var(d(6))
    val d3Signal = d3Var.signal
    val eVar: Var[Set[EffectDieType]] = Var(Set(Mid))
    val eSignal = eVar.signal

    def currFreqs(): Signal[Map[Int, Int]] =
      eSignal.combineWith(d1Signal, d2Signal, d3Signal).map { (e, d1, d2, d3) =>
        freqs(d1, d2, d3, e.toSeq)
      }
    end currFreqs

    def dieUpdater(dieVar: Var[Die]): Observer[Int] =
      dieVar.updater { (_die, n) => d(n) }
    end dieUpdater

    def d1Updater(): Observer[Int] = dieUpdater(d1Var)
    def d2Updater(): Observer[Int] = dieUpdater(d2Var)
    def d3Updater(): Observer[Int] = dieUpdater(d3Var)

    def effectDieTypeUpdater(): Observer[EffectDieType] =
      eVar.updater { (effects, effect) =>
        if effects.contains(effect)
        then effects.filterNot(_ == effect)
        else effects + effect
      }
    end effectDieTypeUpdater
end Model
