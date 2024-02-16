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
              stack = "a"
            },
            new ChartDataSets {
              label = "Roll"
              borderWidth = 1
              backgroundColor = "#00cc00"
              stack = "a"
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

    enum Overcome:
      case BeyondExpectations, Success, MinorTwist, MajorTwist, SpectacularFailure

      def toDescription: String = this match
        case BeyondExpectations => "Beyond Expectations"
        case Success => "Success"
        case MinorTwist => "Minor Twist"
        case MajorTwist => "Major Twist / Failure"
        case SpectacularFailure => "Spectacular Failure"
      end toDescription

      def toClassName: String = this match
        case BeyondExpectations => "beyond-expectations"
        case Success => "success"
        case MinorTwist => "minor-twist"
        case MajorTwist => "major-twist"
        case SpectacularFailure => "spectacular-failure"
      end toClassName
    end Overcome

    def overcomeRoll(n: Int): Overcome =
      if(n >= 12) {
        Overcome.BeyondExpectations
      } else if(n >= 8) {
        Overcome.Success
      } else if(n >= 4) {
        Overcome.MinorTwist
      } else if(n >= 1) {
        Overcome.MajorTwist
      } else {
        Overcome.SpectacularFailure
      }
    end overcomeRoll

    def boostRoll(n: Int): Int =
      if(n >= 12) {
        4
      } else if(n >= 8) {
        3
      } else if(n >= 4) {
        2
      } else if(n >= 1) {
        1
      } else {
        0
      }
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
            input(
              `typ` := "number",
              size := 3,
              controlled(
                value <-- modifierSignal.map(_.toString),
                onInput.mapToValue.map(_.toIntOption).collect { case Some(n) => n } --> { n => modifierVar.update(_ => n) }
              )
            ),
          ),
          td(
            className <-- withModifierSignal.map(_.fold("")(overcomeRoll(_).toClassName)),
            child.text <-- withModifierSignal.map(_.fold("")(overcomeRoll(_).toDescription))
          ),
          td(
            child.text <-- withModifierSignal.map(_.fold("")(Some(_).filter(_ >= 0).getOrElse(0).toString))
          ),
          td(
            child.text <-- withModifierSignal.map(_.fold("")(roll => s"+/-${boostRoll(roll)}"))
          )
        )
      )
    end resultBox

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
          model.currFreqs().combineWith(model.rollForEffectsSignal) --> { (data, roll) =>
            val vals = 1 to data.keys.max
            val labels = vals.map(_.toString)
            val counts = vals.map(data.getOrElse(_, 0).toDouble)
            optChart.foreach { chart =>
              chart.data.labels = labels.toJSArray
              chart.data.datasets.get(0).data = roll.fold(counts.toJSArray){ n =>
                ((counts.take(n - 1) :+ 0.0) ++ counts.drop(n)).toJSArray
              }
              chart.data.datasets.get(1).data = roll.fold(List().toJSArray){ n =>
                ((List.fill(n - 1)(0.0) :+ counts.drop(n-1).head)).toJSArray
              }
              chart.update()
            }
          }
        )
    end renderRollChart

    def renderDice(): Element =
      div(
        dieButtons(model.d1Signal, model.d1Updater(), "Power", _._1),
        dieButtons(model.d2Signal, model.d2Updater(), "Quality", _._2),
        dieButtons(model.d3Signal, model.d3Updater(), "Status", _._3),
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

    def currFreqs(): Signal[Map[Int, Int]] =
      dicePoolSignal.map { (e, d1, d2, d3) =>
        freqs(d1, d2, d3, e.toSeq)
      }
    end currFreqs

    def dieUpdater(dieVar: Var[Die]): Observer[Int] =
      dieVar.updater { (_die, n) =>
        rollVar.update(_ => None)
        d(n)
      }
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
