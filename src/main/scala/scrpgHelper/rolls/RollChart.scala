package scrpgHelper.rolls

import scala.scalajs.js
import scala.scalajs.js.Array
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

object RollChart:
    import Die.*
    import EffectDieType.*
    import scrpgHelper.components.NumBox
    import typings.chartJs.mod.{Chart, ChartDataSets}

    val model: Model = new Model

    def chartConfig(chartDatasets: Array[ChartDataSets]) =
      import typings.chartJs.mod.*

      new ChartConfiguration {
        `type` = ChartType.bar
        data = new ChartData {
          datasets = chartDatasets
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
        h1("Roll Dice"),
        renderDice(),
        renderEffectPanel(),
        diceHolder(),
        resultBox(),
        renderRollChart(),
      )
    end rollChart

    def diceHolder(): Element =
      div(
        className := "dice-holder",
        button(
          tpe := "button",
          "Roll 'em",
          onClick.compose(_.withCurrentValueOf(model.dicePoolSignal)) --> { evPool => evPool match
            case (_, _, d1, d2, d3) => model.rollUpdater.onNext((d1, d2, d3)) }
        ),
        span(
          className := "die-box die-roll",
          child.text <-- model.rollForEffectsSignal.map { roll => roll.fold("0")(_.toString) }
        ),
      )
    end diceHolder

    def dieBox(f: ((Int, Int, Int)) => Int): Element =
        span(
          className := "die-box",
          child.text <-- model.rollSignal.map { roll => roll._1.fold("")(f(_).toString) }
        )
    end dieBox

    def boostRoll(n: Int): Int =
      Overcome.fromNumber(n) match
        case Overcome.SpectacularFailure => 0
        case Overcome.MajorTwist => 1
        case Overcome.MinorTwist => 2
        case Overcome.Success => 3
        case Overcome.BeyondExpectations => 4
    end boostRoll

    def resultBox(): Element =
      val modifierVar: Var[Int] = Var(0)
      val modifierSignal = modifierVar.signal
      val withModifierSignal = model.rollForEffectsSignal.combineWith(modifierSignal).map { rollMod => rollMod match
        case (Some(n), m) => Some(n + m)
        case (None, _) => None
      }

      table(
        className := "die-result",
        tr(th("Modifier"), th("Overcome"), th("Attack / Defend"), th("Boost / Hinder")),
        tr(
          td(
            NumBox(modifierSignal, modifierVar.updater { (n, x) => n + x }, modifierVar.updater { (_, m) => m })
              .render()
          ),
          td(
            className <-- withModifierSignal.map(_.fold("")(Overcome.fromNumber(_).toClassName)),
            child.text <-- withModifierSignal.map(_.fold("")(Overcome.fromNumber(_).toDescription))
          ),
          td(
            child.text <-- withModifierSignal.map(_.fold("")(Some(_).filter(_ >= 0).getOrElse(0).toString))
          ),
          td(
            child.text <-- withModifierSignal.map(_.fold("")(roll => s"±${boostRoll(roll)}"))
          )
        ),
        tr(
          td(
            colSpan := 4,
            renderEffectChart(modifierSignal)
          )
        )
      )
    end resultBox

    def renderChart[A](datasets: Array[ChartDataSets], signal: Signal[A], f: (Option[Chart], Map[Int,Int], A) => Unit): Element =
      val showChart: Var[Boolean] = Var(true)
      val showChartSignal = showChart.signal

      var optChart: Option[Chart] = None
      div(
        className := "chart",
        button(
          className := "show-chart-button",
          child.text <-- showChartSignal.map(s => if s then "▵ Hide" else "▿ Show"),
          onClick --> { _ => showChart.update(!_) },
        ),
        div(
          className <-- showChartSignal.map(s => s"show-chart-$s"),
          canvasTag(
            width := "100%",
            height := "200px",
            onMountUnmountCallback(
              mount = { nodeCtx =>
                val domCanvas: dom.HTMLCanvasElement = nodeCtx.thisNode.ref
                val chart = Chart.apply.newInstance2(domCanvas, chartConfig(datasets))
                optChart = Some(chart)
              },
              unmount = { thisNode =>
                optChart.foreach(_.destroy())
                optChart = None
              },
            ),
            model.currFreqs.combineWith(signal) --> { (data, signalVal) =>
              f(optChart, data, signalVal)
            }
          )
        )
      )
    end renderChart

    def renderRollChart(): Element =
      import scala.scalajs.js.JSConverters.*

      val datasets = js.Array(
            new ChartDataSets {
              label = "Count"
              borderWidth = 1
              backgroundColor = "#cccccc"
              stack = "a"
            },
            new ChartDataSets {
              label = "Roll"
              borderWidth = 1
              backgroundColor = "#00cc00"
              stack = "a"
            },
          )

      renderChart(datasets,
                  model.rollForEffectsSignal,
                  { (optChart, data, roll) =>
        val vals = 1 to data.keys.max
        val labels = vals.map(_.toString)
        val counts = vals.map(data.getOrElse(_, 0).toDouble)
        optChart.foreach { chart =>
          chart.data.labels = labels.toJSArray
          chart.data.datasets.get(0).data = roll.fold(counts.toJSArray){ n =>
            ((counts.take(n - 1) :+ 0.0) ++ counts.drop(n)).toJSArray
          }
          chart.data.datasets.get(1).data = roll.fold(List().toJSArray){ n =>
            ((List.fill(n - 1)(0.0) :+ counts.drop(n-1).headOption.getOrElse(0.0))).toJSArray
          }
          chart.update()
        }
      })
    end renderRollChart

    def getCountsForBin(n: Int, data: Map[Int, Int], modifier: Int): Int =
      val range = if(n == 1) {
        0 to -modifier
      } else if(n == 2) {
        (1 - modifier) to (3 - modifier)
      } else if(n == 3) {
        (4 - modifier) to (7 - modifier)
      } else if(n == 4) {
        (8 - modifier) to (11 - modifier)
      } else {
        (12 - modifier) to data.keys.max
      }
      range.map(n => data.getOrElse(n, 0)).sum
    end getCountsForBin

    def renderEffectChart(modifierSignal: Signal[Int]): Element =
        import scala.scalajs.js.JSConverters.*

        val datasets = js.Array(
            new ChartDataSets {
              label = "Outcome"
              borderWidth = 1
              backgroundColor = "#cccccc"
              stack = "a"
            },
            new ChartDataSets {
              label = "Roll"
              borderWidth = 1
              backgroundColor = "#0000aa"
              stack = "a"
            },
          )

        renderChart(datasets,
                    model.rollForEffectsSignal.combineWith(modifierSignal),
                    { case (optChart, data, (roll, modifier)) =>
                      val vals = 1 to 5
                      val labels = vals.map(n => if(n == 1) {
                                              "Spectacular Failure / ±0"
                                            } else if(n == 2) {
                                              "Major Twist / Failure / ±1"
                                            } else if(n == 3) {
                                              "Minor Twist / ±2"
                                            } else if(n == 4) {
                                              "Success / ±3"
                                            } else {
                                              "Beyond Expectations / ±4"
                                            })
                      val counts = vals.map(n => getCountsForBin(n, data, modifier).toDouble)
                      optChart.foreach { chart =>
                        chart.data.labels = labels.toJSArray
                        chart.data.datasets.get(0).data = roll.fold(counts.toJSArray){ n =>
                          val m = Overcome.fromNumber(n + modifier).ordinal + 1
                          ((counts.take(m - 1) :+ 0.0) ++ counts.drop(m)).toJSArray
                        }
                        chart.data.datasets.get(1).data = roll.fold(List().toJSArray){ n =>
                          val m = Overcome.fromNumber(n + modifier).ordinal + 1
                          ((List.fill(m - 1)(0.0) :+ counts.drop(m-1).headOption.getOrElse(0.0))).toJSArray
                        }
                        chart.update()
                      }
                    }
        )
    end renderEffectChart

    def renderDice(): Element =
      div(
        dieButtons(model.d1Signal, model.d1Updater, "Power", _._1),
        dieButtons(model.d2Signal, model.d2Updater, "Quality", _._2),
        dieButtons(model.d3Signal, model.d3Updater, "Status", _._3),
      )
    end renderDice

    def dieButtons(dieSignal: Signal[Die],
                   dieObserver: Observer[Int],
                   label: String,
                   rollChooser: ((Int, Int, Int)) => Int): Element =
      div(
        className := "dicegroup",
        h3(label),
        div(dieBox(rollChooser)),
        div(
          dieButton(dieSignal, dieObserver, 4),
          dieButton(dieSignal, dieObserver, 6),
          dieButton(dieSignal, dieObserver, 8),
          dieButton(dieSignal, dieObserver, 10),
          dieButton(dieSignal, dieObserver, 12),
        )
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
      effectPanel(model.eSignal, model.effectDieTypeUpdater)
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

    val dicePoolSignal = eSignal.combineWith(d1Signal, d2Signal, d3Signal)

    val rollVar: Var[Option[(Int, Int, Int)]] = Var(None)
    val rollSignal = rollVar.signal.combineWith(eSignal.map(_.toSeq))

    val rollForEffectsSignal = rollSignal.map { roll => roll match
      case (Some((n, m, l)), e) => Some(Die.fromEffects(n, m, l, e))
      case (None, _) => None
    }

    val rollUpdater: Observer[(Die, Die, Die)] = rollVar.updater { (_, pool) => pool match
      case (d1, d2, d3) => Some(d1.roll(), d2.roll(), d3.roll())
    }

    val currFreqs: Signal[Map[Int, Int]] =
      var memo: Map[(Set[EffectDieType], Set[Die]), Map[Int, Int]] = Map()
      dicePoolSignal.map { (e, d1, d2, d3) =>
        val k = (e.toSet, Set(d1, d2, d3))
        memo.getOrElse(k, {
          val res = freqs(d1, d2, d3, e.toSeq)
          memo = memo + (k -> res)
          res
        })
      }
    end currFreqs

    def dieUpdater(dieVar: Var[Die]): Observer[Int] =
      dieVar.updater { (_die, n) =>
        rollVar.update(_ => None)
        d(n)
      }
    end dieUpdater

    val d1Updater: Observer[Int] = dieUpdater(d1Var)
    val d2Updater: Observer[Int] = dieUpdater(d2Var)
    val d3Updater: Observer[Int] = dieUpdater(d3Var)

    val effectDieTypeUpdater: Observer[EffectDieType] =
      eVar.updater { (effects, effect) =>
        if effects.contains(effect)
        then effects.filterNot(_ == effect)
        else effects + effect
      }
    end effectDieTypeUpdater
end Model
