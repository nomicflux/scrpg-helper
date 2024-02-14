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

    val dataVar: Var[(Die, Die, Die, Seq[EffectDieType])] = Var((d(4), d(8), d(12), Seq(Min, Max)))
    val dataSignal  = dataVar.signal

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
        renderRollChart())
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
          dataSignal --> { (d1, d2, d3, e) =>
            val data = freqs(d1, d2, d3, e)
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
end Main
