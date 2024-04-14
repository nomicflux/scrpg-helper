package scrpgHelper.chargen
import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die

object RenderArchetype:
  import scrpgHelper.components.SelectWithPrevChoice

  val model = new ArchetypeModel()

  def renderArchetypes(character: CharacterModel): Element =
    div(
      className := "archetype-section choice-section",
      h2("Archetype"),
      renderRollButton(model.rollTrigger, character),
      renderShownToggle(model.showUnchosenSignal, model.shownToggle),
      renderArchetypeTable(character)
    )

  def renderRollButton(
      rollTrigger: Observer[List[Die]],
      character: CharacterModel
  ): Element =
    div(
      button(
        tpe := "button",
        "Roll",
        onClick
          .compose(
            _.withCurrentValueOf(
              character.powerSourceSignal.map(
                _.toList.flatMap(_.archetypeDiePool)
              )
            )
          ) --> { (_, dice) =>
          rollTrigger.onNext(dice)
        }
      )
    )
  end renderRollButton

  def renderShownToggle(
      shown: Signal[Boolean],
      shownToggle: Observer[Unit]
  ): Element =
    div(
      button(
        tpe := "button",
        child.text <-- shown.map(b => if b then "Hide" else "Show"),
        " Archetypes",
        onClick --> { _ => shownToggle.onNext(()) }
      )
    )

  def renderArchetypeTable(character: CharacterModel): Element =
    table(
      tr(
        th(),
        th("Archetype"),
        th("Powers"),
        th("Qualities"),
        th(colSpan := 2, "Principle of"),
        th("Abilities")
      ),
      Archetype.archetypes.map(renderArchetypeRow(character, _))
    )

  def renderArchetypeRow(
      character: CharacterModel,
      archetype: Archetype
  ): Element =
    tr(
      className := "archetype-row",
      className <-- model.rollsSignal
        .combineWith(model.showUnchosenSignal)
        .map { (mrolls, shown) =>
          mrolls.fold("undecided") { rolls =>
            if (rolls.contains(archetype.number)) {
              "chosen"
            } else {
              if shown then "unchosen" else "hidden"
            }
          }
        },
      className <-- character.archetypeSignal.map(mat =>
        if mat.fold(false)(_ == archetype) then "picked" else "unpicked"
      ),
      td(
        archetype.number.toString
      ),
      td(
        h3(archetype.name)
      ),
      td(
        renderPowers(
          character.powerSourceSignal.map(mps =>
            mps.fold(List())(_.archetypeDiePool)
          ),
          archetype.powerList,
          character.powersSignal(Signal.fromValue(Some(archetype)))
            .combineWith(character.powerSourceSignal.map(mps => mps.toList.flatMap(_.archetypeDiePool)))
            .map{ (l, ds) =>
            val powerSet: Set[Power] = l.map(_._1).toSet
            (pd, mpc) => powerSet.contains(pd._1) || !mandatoryPowerCheck(ds, archetype)(l)(pd, mpc)
          },
          character.removePower(archetype),
          character.addPower(archetype),
        )
      ),
      td(
        renderQualities(
          character.powerSourceSignal.map(mps =>
            mps.fold(List())(_.archetypeDiePool)
          ),
          archetype.qualityList,
          character.qualitiesSignal(Signal.fromValue(Some(archetype)))
            .combineWith(character.powerSourceSignal.map(mps => mps.toList.flatMap(_.archetypeDiePool)))
            .map{ (l, ds) =>
            val qualitySet: Set[Quality] = l.map(_._1).toSet
            (qd, mpc) => qualitySet.contains(qd._1) || !mandatoryQualityCheck(ds, archetype)(l)(qd, mpc)
          },
          character.removeQuality(archetype),
          character.addQuality(archetype),
        )
      ),
      td(s"(${archetype.principleCategory.toString})"),
      td(
        renderPrinciples(
          Principle.categoryToPrinciples(archetype.principleCategory),
          character
            .abilitiesSignal(character.archetypeSignal)
            .combineWith(
              character.abilitiesSignal(character.powerSourceSignal),
              character.abilitiesSignal(character.backgroundSignal),
            )
            .map { (ats, pss, bgs) =>
              val abilitySet = (ats.map(_.id) ++ pss.map(_.id) ++ bgs.map(_.id)).toSet
              a => abilitySet.contains(a.id)
            },
          character.removeAbility(archetype),
          character.addAbility(archetype)
        )
      ),
      td(
        archetype.abilityPools.map { ap =>
          RenderAbility.renderAbilityPool(character, archetype, ap)
        }
      ),
      onMouseDown --> { _ =>
        character.changeArchetype.onNext(archetype)
      },
      onFocus --> { _ =>
        character.changeArchetype.onNext(archetype)
      },
      onClick --> { _ =>
        character.changeArchetype.onNext(archetype)
      }
    )

  def mandatoryPowerCheck(dicePool: List[Die], archetype: Archetype)(
      pds: List[(Power, Die)]
  )(pd: (Power, Die), prevChoice: Option[(Power, Die)]): Boolean =
    val baseSet = pds.map(_._1).toSet
    val newSet = prevChoice.fold(baseSet)(pc => baseSet - pc._1) + pd._1
    archetype.powerValidation(newSet) || newSet.size < dicePool.size
  end mandatoryPowerCheck

  def mandatoryQualityCheck(dicePool: List[Die], archetype: Archetype)(
      qds: List[(Quality, Die)]
  )(pd: (Quality, Die), prevChoice: Option[(Quality, Die)]): Boolean =
    val baseSet = qds.map(_._1).toSet
    val newSet = prevChoice.fold(baseSet)(pc => baseSet - pc._1) + pd._1
    archetype.qualityValidation(newSet) || newSet.size < dicePool.size
  end mandatoryQualityCheck

  def renderQualities(
    dicePool: Signal[List[Die]],
    qualities: List[Quality],
    qualityAllowed: Signal[((Quality, Die), Option[(Quality, Die)]) => Boolean],
    removeQuality: Observer[(Quality, Die)],
    addQuality: Observer[(Quality, Die)],
  ): Element =
    div(
      children <-- dicePool.map { ds =>

      ds.map { d =>
        span(
          className := "choice-die-box",
          d.toString,
          ":",
          SelectWithPrevChoice(qualities.map(q => (q, d)), qd => qd._1.name)
            .render(qualityAllowed,
                    removeQuality,
                    addQuality)
        )
      }
      }
    )
  end renderQualities

  def renderPowers(
      dicePool: Signal[List[Die]],
      powers: List[Power],
      powerAllowed: Signal[((Power, Die), Option[(Power, Die)]) => Boolean],
      removePower: Observer[(Power, Die)],
      addPower: Observer[(Power, Die)]
  ): Element =
    div(
      children <-- dicePool
        .map(ds =>
          ds.map { d =>
            span(
              className := "choice-die-box",
              d.toString,
              ":",
              SelectWithPrevChoice[(Power, Die)](
                powers.map(p => (p, d)),
                pd => pd._1.name
              )
                .render(powerAllowed, removePower, addPower)
            )
          }
        )
    )
  end renderPowers


  def renderPrinciples(
      principles: List[Principle],
      abilityAllowed: Signal[Principle => Boolean],
      removePrinciple: Observer[Principle],
      addPrinciple: Observer[Principle]
  ): Element =
    SelectWithPrevChoice(principles, p => p.name)
      .render(
        abilityAllowed.map(f => (a, _) => f(a)),
        removePrinciple,
        addPrinciple
      )
  end renderPrinciples

end RenderArchetype

final class ArchetypeModel:
  import scrpgHelper.rolls.Die.d

  val rolls: Var[Option[Set[Int]]] = Var(None)
  val rollsSignal = rolls.signal
  val showUnchosen: Var[Boolean] = Var(false)
  val showUnchosenSignal = showUnchosen.signal

  val rollTrigger: Observer[List[Die]] = rolls.updater { (_, ds) =>
    Some(Die.rollForCharGen(ds))
  }

  val shownToggle: Observer[Unit] = showUnchosen.updater { (b, _) => !b }
end ArchetypeModel
