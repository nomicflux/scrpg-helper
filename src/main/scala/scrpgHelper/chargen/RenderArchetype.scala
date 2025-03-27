package scrpgHelper.chargen
import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die
import scrpgHelper.chargen.archetypes.Speedster

object RenderArchetype:
  import scrpgHelper.components.SelectWithPrevChoice

  val model = new ArchetypeModel()

  def renderArchetypes(character: CharacterModel): Element =
    div(
      className := "archetype-section choice-section",
      h2("Archetype"),
      RollComponent.renderRollButton(
        model.rollTrigger,
        character.powerSource.signal.map(
          _.toList.flatMap(_.archetypeDiePool)
        )
      ),
      RollComponent.renderShownToggle(
        model.rollsSignal,
        model.showUnchosenSignal,
        model.shownToggle,
        "Archetypes"
      ),
      renderArchetypeTable(character)
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
    val qualityDisallowed:Signal[
        (
            Option[(Quality, Die)],
            Option[(Power, Die)],
            Option[(Quality, Die)]
        ) => Boolean
      ]  = character
            .qualitiesSignal(Signal.fromValue(Some(archetype)))
            .combineWith(
              character.qualitiesSignal(character.powerSource.signal),
              character.qualitiesSignal(character.background.signal),
              character.powersSignal(Signal.fromValue(Some(archetype))),
              character.powerSource.signal
                .map(_.toList.flatMap(_.archetypeDiePool))
            )
            .map { (qs, psqs, bgqs, ps, ds) =>
              val qualitySet: Set[Quality] =
                (qs ++ psqs ++ bgqs).map(_._1).toSet
              { (mqd, mpd, mpc) =>
                val alreadyHave =
                  mqd.fold(false)(qd => qualitySet.contains(qd._1))
                val wouldFail = !mandatoryQualityCheck(
                  ds,
                  archetype
                )(qs, psqs ++ bgqs, ps)(mqd, mpd, mpc)
                alreadyHave || wouldFail
              }
            }
    val powerDisallowed: Signal[
        (
            Option[(Power, Die)],
            Option[(Quality, Die)],
            Option[(Power | Quality, Die)]
        ) => Boolean
      ] = character
            .powersSignal(Signal.fromValue(Some(archetype)))
            .combineWith(
              character.powersSignal(character.powerSource.signal),
              character.qualitiesSignal(Signal.fromValue(Some(archetype))),
              character.powerSource.signal
                .map(_.toList.flatMap(_.archetypeDiePool))
            )
            .map { (ps, psps, qs, ds) =>
              val powerSet: Set[Power] = (ps ++ psps).map(_._1).toSet
              { (mpd, mqd, mpc) =>
                val alreadyHave =
                  mpd.fold(false)(pd => powerSet.contains(pd._1))
                val wouldFail = !mandatoryPowerCheck(ds, archetype)(
                  ps,
                  psps,
                  qs
                )(mpd, mqd, mpc)
                alreadyHave || wouldFail
              }
            }
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
      className <-- character.archetype.signal.map(mat =>
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
          character.powerSource.signal.map(mps =>
            mps.fold(List())(_.archetypeDiePool)
          ),
          archetype.powerList,
          archetype.qualityList,
          powerDisallowed,
          qualityDisallowed,
          character.removePower(archetype),
          character.addPower(archetype),
          character.removeQuality(archetype),
          character.addQuality(archetype)
        ),
        renderPowerQualities(
          character.powerSource.signal.map(mps =>
            mps.fold(List())(ps => if ps.selectExtraArchetypeQuality then List(Die.d(10)) else List())
          ),
          List(),
          archetype.qualityList,
          powerDisallowed,
          qualityDisallowed,
          character.removePower(archetype),
          character.addPower(archetype),
          character.removeQuality(archetype),
          character.addQuality(archetype)
        ),
        renderPowerQualities(
          Signal.fromValue(archetype.extraPowers._1),
          archetype.extraPowers._2,
          List(),
          powerDisallowed,
          qualityDisallowed,
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
            .abilitiesSignal(character.archetype.signal)
            .combineWith(
              character.abilitiesSignal(character.powerSource.signal),
              character.abilitiesSignal(character.background.signal)
            )
            .map { (ats, pss, bgs) =>
              val abilitySet =
                (ats.map(_.key) ++ pss.map(_.key) ++ bgs.map(_.key)).toSet
              a => abilitySet.contains(a.key)
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
  )(
      pd: Option[(Power, Die)],
      qd: Option[(Quality, Die)],
      prevChoice: Option[(Power | Quality, Die)]
  ): Boolean =
    val baseSet = pds.map(_._1).toSet
    val newSet =
      prevChoice
        .map(_._1)
        .collect { case p: Power => p }
        .fold(baseSet)(baseSet - _)
        .union(pd.toSet.map(_._1))
    val newSetWithOld = newSet.union(pspds.map(_._1).toSet)
    val allPowersCheckOut = archetype.powerValidation(newSetWithOld)
    val haveEnough = newSet.size >= archetype.minPowers
    val qualSize = prevChoice.map(_._1).collect { case q: Quality => q }.size
    val notFullYet =
      (newSet.size + qds.size + qd.size - qualSize) < dicePool.size
    notFullYet || (allPowersCheckOut && haveEnough)
  end mandatoryPowerCheck

  def mandatoryQualityCheck(dicePool: List[Die], archetype: Archetype)(
      qds: List[(Quality, Die)],
      prevQds: List[(Quality, Die)],
      pds: List[(Power, Die)]
  )(
      qd: Option[(Quality, Die)],
      pd: Option[(Power, Die)],
      prevChoice: Option[(Quality, Die)]
  ): Boolean =
    val baseSet = qds.map(_._1).toSet
    val newSet =
      prevChoice.fold(baseSet)(baseSet - _._1).union(qd.toSet.map(_._1))
    val newSetWithOld = newSet.union(prevQds.map(_._1).toSet)
    val allQualitiesCheckOut = archetype.qualityValidation(newSetWithOld)
    val haveEnough = (pds.size + pd.size) >= archetype.minPowers
    val notFullYet = (newSet.size + pds.size + pd.size) < dicePool.size
    (allQualitiesCheckOut && haveEnough) || notFullYet
  end mandatoryQualityCheck

  def renderPowerQualities(
      dicePool: Signal[List[Die]],
      powers: List[Power],
      qualities: List[Quality],
      powerDisallowed: Signal[
        (
            Option[(Power, Die)],
            Option[(Quality, Die)],
            Option[(Power | Quality, Die)]
        ) => Boolean
      ],
      qualityDisallowed: Signal[
        (
            Option[(Quality, Die)],
            Option[(Power, Die)],
            Option[(Quality, Die)]
        ) => Boolean
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
      powerDisallowed: Signal[
        (
            Option[(Power, Die)],
            Option[(Quality, Die)],
            Option[(Power | Quality, Die)]
        ) => Boolean
      ],
      qualityDisallowed: Signal[
        (
            Option[(Quality, Die)],
            Option[(Power, Die)],
            Option[(Quality, Die)]
        ) => Boolean
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
        .map { (pa, qa) => (pq, mpc) =>
          val pz = pa(Some(pq), None, mpc)
          val qz = qa(None, Some(pq), None)
          pz || qz
        }
    val qualityDisallowedWithPowers
        : Signal[((Quality, Die), Option[(Quality, Die)]) => Boolean] =
      qualityDisallowed
        .combineWith(powerDisallowed)
        .map { (qa, pa) =>
          { (qd, mpc) =>
            qa(Some(qd), None, mpc) || pa(None, Some(qd), mpc)
          }
        }

    div(
      className := "choice-die-box",
      className <-- chosen.signal.map { spq => if spq.isEmpty then "power-quality-list" else "" },
      die.toString,
      ": ",
      span(
        className := "power-list",
        className <-- chosen.signal.map { spq =>
          spq.fold(if powers.isEmpty then "hidden" else "")(pq =>
            pq match
              case q: Quality => "hidden"
              case _          => ""
          )
        },
        SelectWithPrevChoice(powers.map(p => (p, die)), pd => pd._1.name)
          .render(
            powerDisallowedWithQualities,
            removePowerWithChoice,
            addPowerWithChoice
          )
      ),
      span(
        className := "pq-list-separator",
        className <-- chosen.signal.map { spq => if spq.isDefined || powers.isEmpty || qualities.isEmpty then "hidden" else "" },
        "-or-"
      ),
      span(
        className := "quality-list",
        className <-- chosen.signal.map { spq =>
          spq.fold(if qualities.isEmpty then "hidden" else "")(pq =>
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