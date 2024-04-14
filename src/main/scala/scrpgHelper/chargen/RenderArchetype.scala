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
        th("Powers/Qualities"),
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
        renderPowerQualities(
          character.powerSourceSignal.map(mps =>
            mps.fold(List())(_.archetypeDiePool)
          ),
          archetype.powerList,
          archetype.qualityList,
          character
            .powersSignal(Signal.fromValue(Some(archetype)))
            .combineWith(
              character.powersSignal(character.powerSourceSignal),
              character.qualitiesSignal(Signal.fromValue(Some(archetype))),
              character.powerSourceSignal.map(_.toList.flatMap(_.archetypeDiePool))
            )
            .map { (ps, psps, qs, ds) =>
              val powerSet: Set[Power] = (ps ++ psps).map(_._1).toSet
              { (mpd, mqd, mpc) =>
                val alreadyHave = mpd.fold(false)(pd => powerSet.contains(pd._1))
                val wouldFail = !mandatoryPowerCheck(ds, archetype)(
                  ps, psps, qs
                )(mpd, mqd, mpc)
                alreadyHave || wouldFail
              }
            },
          character
            .qualitiesSignal(Signal.fromValue(Some(archetype)))
            .combineWith(
              character.qualitiesSignal(character.powerSourceSignal),
              character.qualitiesSignal(character.backgroundSignal),
              character.powersSignal(Signal.fromValue(Some(archetype))),
              character.powerSourceSignal.map(_.toList.flatMap(_.archetypeDiePool))
            )
            .map { (qs, psqs, bgqs, ps, ds) =>
              val qualitySet: Set[Quality] = (qs ++ psqs ++ bgqs).map(_._1).toSet
              { (mqd, mpd, mpc) =>
                val alreadyHave = mqd.fold(false)(qd => qualitySet.contains(qd._1))
                val wouldFail = !mandatoryQualityCheck(
                  ds,
                  archetype
                )(qs, psqs ++ bgqs, ps)(mqd, mpd, mpc)
                alreadyHave || wouldFail
              }
            },
          character.removePower(archetype),
          character.addPower(archetype),
          character.removeQuality(archetype),
          character.addQuality(archetype)
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
              character.abilitiesSignal(character.backgroundSignal)
            )
            .map { (ats, pss, bgs) =>
              val abilitySet =
                (ats.map(_.id) ++ pss.map(_.id) ++ bgs.map(_.id)).toSet
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
      pds: List[(Power, Die)],
      pspds: List[(Power, Die)],
      qds: List[(Quality, Die)]
  )(pd: Option[(Power, Die)], qd: Option[(Quality, Die)], prevChoice: Option[(Power, Die)]): Boolean =
    val baseSet = pds.map(_._1).toSet
    val newSet = prevChoice.fold(baseSet)(baseSet - _._1).union(pd.toSet.map(_._1))
    val newSetWithOld = newSet.union(pspds.map(_._1).toSet)
    val allPowersCheckOut = archetype.powerValidation(newSetWithOld)
    val haveEnough = newSet.size >= archetype.minPowers
    val notFullYet = (newSet.size + qds.size + qd.size) < dicePool.size
    notFullYet || (allPowersCheckOut && haveEnough)
  end mandatoryPowerCheck

  def mandatoryQualityCheck(dicePool: List[Die], archetype: Archetype)(
      qds: List[(Quality, Die)],
      prevQds: List[(Quality, Die)],
      pds: List[(Power, Die)]
  )(qd: Option[(Quality, Die)], pd: Option[(Power, Die)], prevChoice: Option[(Quality, Die)]): Boolean =
    val baseSet = qds.map(_._1).toSet
    val newSet = prevChoice.fold(baseSet)(baseSet - _._1).union(qd.toSet.map(_._1))
    val newSetWithOld = newSet.union(prevQds.map(_._1).toSet)
    (archetype.qualityValidation(newSetWithOld) && pds.size >= archetype.minPowers) || (newSet.size + pds.size + pd.size) < dicePool.size
  end mandatoryQualityCheck

  def renderPowerQualities(
      dicePool: Signal[List[Die]],
      powers: List[Power],
      qualities: List[Quality],
      powerDisallowed: Signal[(Option[(Power, Die)], Option[(Quality, Die)], Option[(Power, Die)]) => Boolean],
      qualityDisallowed: Signal[
        (Option[(Quality, Die)], Option[(Power, Die)], Option[(Quality, Die)]) => Boolean
      ],
      removePower: Observer[(Power, Die)],
      addPower: Observer[(Power, Die)],
      removeQuality: Observer[(Quality, Die)],
      addQuality: Observer[(Quality, Die)]
  ): Element =
    div(
      children <-- dicePool.map { ds =>
        ds.map { d =>
          renderPowerQuality(
            d,
            powers,
            qualities,
            powerDisallowed,
            qualityDisallowed,
            removePower,
            addPower,
            removeQuality,
            addQuality
          )
        }
      }
    )
  end renderPowerQualities

  def renderPowerQuality(
      die: Die,
      powers: List[Power],
      qualities: List[Quality],
      powerDisallowed: Signal[(Option[(Power, Die)], Option[(Quality, Die)], Option[(Power, Die)]) => Boolean],
      qualityDisallowed: Signal[
        (Option[(Quality, Die)], Option[(Power, Die)], Option[(Quality, Die)]) => Boolean
      ],
      removePower: Observer[(Power, Die)],
      addPower: Observer[(Power, Die)],
      removeQuality: Observer[(Quality, Die)],
      addQuality: Observer[(Quality, Die)]
  ): Element =
    val chosen: Var[Option[Power | Quality]] = Var(None)
    val addPowerWithChoice: Observer[(Power, Die)] =
      addPower.contramap { pd =>
        chosen.update(_ => Some(pd._1))
        pd
      }
    val removePowerWithChoice: Observer[(Power, Die)] =
      removePower.contramap { pd =>
        chosen.update(_ => None)
        pd
      }
    val addQualityWithChoice: Observer[(Quality, Die)] =
      addQuality.contramap { qd =>
        chosen.update(_ => Some(qd._1))
        qd
      }
    val removeQualityWithChoice: Observer[(Quality, Die)] =
      removeQuality.contramap { qd =>
        chosen.update(_ => None)
        qd
      }
    val powerDisallowedWithQualities
        : Signal[((Power, Die), Option[(Power, Die)]) => Boolean] =
      powerDisallowed
        .combineWith(qualityDisallowed)
        .map { (pa, qa) =>
          (pq, mpc) => pa(Some(pq), None, mpc) || qa(None, Some(pq), None)
        }
    val qualityDisallowedWithPowers
        : Signal[((Quality, Die), Option[(Quality, Die)]) => Boolean] =
      qualityDisallowed
        .combineWith(powerDisallowed)
        .map { (qa, pa) =>
          { (qd, mpc) =>
            qa(Some(qd), None, mpc) || pa(None, Some(qd), None)
          }
        }

    div(
      className := "choice-die-box",
      die.toString,
      ":",
      span(
        className <-- chosen.signal.map { spq =>
          spq.fold("")(pq =>
            pq match
              case q: Quality => "hidden"
              case _          => ""
          )
        },
        SelectWithPrevChoice(powers.map(p => (p, die)), pd => pd._1.name)
          .render(powerDisallowedWithQualities, removePowerWithChoice, addPowerWithChoice)
      ),
      span(
        className <-- chosen.signal.map { spq =>
          spq.fold("")(pq =>
            pq match
              case p: Power => "hidden"
              case _        => ""
          )
        },
        SelectWithPrevChoice(qualities.map(q => (q, die)), qd => qd._1.name)
          .render(
            qualityDisallowedWithPowers,
            removeQualityWithChoice,
            addQualityWithChoice
          )
      )
    )
  end renderPowerQuality

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
