package scrpgHelper.chargen

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.components.SelectWithPrevChoice

object RenderAbility:
  def renderAbilityPool(
      character: CharacterModel,
      stagingKey: character.StagingKey,
      abilityPool: AbilityPool
  ): Element =
    div(
      className := "ability-pool",
      span(s"Pick ${abilityPool.max}:"),
      div(
        abilityPool.abilities.map(
          renderAbility(
            character,
            stagingKey,
            abilityPool,
            _,
            character.abilityChoicesSignal(stagingKey)
          )
        )
      )
    )
  end renderAbilityPool

  def renderAbility(
      character: CharacterModel,
      stagingKey: character.StagingKey,
      abilityPool: AbilityPool,
      template: AbilityTemplate,
      chosenSignal: Signal[Map[AbilityTemplate, ChosenAbility]]
  ): Element =
    val chosenAbility = chosenSignal.map(_.get(template).head)
    val hovering: Var[Boolean] = Var(false)

    div(
      className := s"ability status-${template.status.toString.toLowerCase()}",
      className <-- hovering.signal.map(b =>
        if b then "ability-hover" else "ability-blur"
      ),
      className <-- character
        .abilitiesSignal(Signal.fromValue(Some(stagingKey)))
        .map(l =>
          if l.collect { case ca: ChosenAbility => ca.template }
              .contains(template)
          then "ability-selected"
          else "ability-unselected"
        ),
      className <-- character
        .abilitiesSignal(Signal.fromValue(Some(stagingKey)))
        .map(l =>
          if !l.collect { case ca: ChosenAbility => ca.template }
            .filter(at => at.id == template.id && at.status != template.status)
            .isEmpty
          then "hidden"
          else ""
        ),
      span(
        className := "ability-actions",
        child.text <-- chosenAbility.map(
          _.actions.map(_.toSymbol).foldLeft("")(_ + _)
        )
      ),
      span(
        className := "ability-name",
        child.text <-- chosenAbility.map(_.name)
      ),
      span(
        className := "ability-category",
        child.text <-- chosenAbility.map(_.category.toAbbreviation)
      ),
      span(
        className := "ability-description",
        renderDescription(
          character,
          stagingKey,
          template,
          chosenSignal.map(
            _.values.toList.filter(_.inPool.id == abilityPool.id)
          )
        )
      ),
      onMouseOver --> { _ => hovering.update { _ => true } },
      onMouseOut --> { _ => hovering.update { _ => false } },
      onBlur --> { _ => hovering.update { _ => false } },
      onClick.compose(_.withCurrentValueOf(chosenAbility)) --> { (_, chosen) =>
        character.toggleAbility(stagingKey).onNext(chosen)
      }
    )
  end renderAbility

  def renderDescription(
      character: CharacterModel,
      stagingKey: character.StagingKey,
      ability: AbilityTemplate,
      chosen: Signal[List[ChosenAbility]]
  ): Element =
    val powerSignal: Signal[List[Power]] = stagingKey match
        case bg: Background => Signal.fromValue(List.empty)
        case ps: PowerSource => character.powersSignal(Signal.fromValue(Some(ps))).map(_.map(_._1))
        case at: Archetype => character.powersSignal(character.powerSourceSignal)
            .combineWith(character.powersSignal(Signal.fromValue(Some(at))))
            .map(_.map(_._1) ++ _.map(_._1))
        case p: Personality => character.powersSignal(character.powerSourceSignal)
            .combineWith(character.powersSignal(character.archetypeSignal))
            .map(_.map(_._1) ++ _.map(_._1))

    val qualitySignal: Signal[List[Quality]] = stagingKey match
        case bg: Background => character.qualitiesSignal(Signal.fromValue(Some(bg))).map(_.map(_._1))
        case ps: PowerSource => character.qualitiesSignal(character.backgroundSignal)
            .combineWith(character.qualitiesSignal(Signal.fromValue(Some(ps))))
            .map(_.map(_._1) ++ _.map(_._1))
        case at: Archetype => character.qualitiesSignal(character.backgroundSignal)
            .combineWith(
              character.qualitiesSignal(character.powerSourceSignal),
              character.qualitiesSignal(Signal.fromValue(Some(at))),
            )
            .map(_.map(_._1) ++ _.map(_._1) ++ _.map(_._1))
        case p: Personality => character.qualitiesSignal(character.archetypeSignal)
            .combineWith(character.qualitiesSignal(character.powerSourceSignal),
                         character.qualitiesSignal(character.backgroundSignal),
                         character.qualitiesSignal(Signal.fromValue(Some(p))))
            .map(_.map(_._1) ++ _.map(_._1) ++ _.map(_._1) ++ _.map(_._1))

    span(
      ability.description.map { l =>
        l match
          case s: String => span(s)
          case ec: EnergyChoice =>
            renderEnergyChoices(character, stagingKey, chosen, ability, ec)
          case ac: ActionChoice =>
            renderActionChoices(character, stagingKey, chosen, ability, ac)
          case pc: PowerChoice =>
            renderPowerChoices(
              character,
              stagingKey,
              powerSignal,
              chosen,
              ability,
              pc
            )
          case qc: QualityChoice =>
            renderQualityChoices(
              character,
              stagingKey,
              qualitySignal,
              chosen,
              ability,
              qc
            )
          case pqc: PowerQualityChoice =>
            renderPowerQualityChoices(
              character,
              stagingKey,
              powerSignal,
              qualitySignal,
              chosen,
              ability,
              pqc
            )
          case _: AbilityChoice => span("Not valid")
      }
    )
  end renderDescription

  def renderEnergyChoices(
      character: CharacterModel,
      stagingKey: character.StagingKey,
      chosen: Signal[List[ChosenAbility]],
      ability: AbilityTemplate,
      ec: EnergyChoice
  ): Element =
    span(
      onMouseOver --> { ev => ev.stopPropagation() },
      onClick --> { ev => ev.stopPropagation() },
      SelectWithPrevChoice[Energy](
        Energy.values.toList.filter(e => ec.validateFn(List(e))),
        e => e.toString
      )
        .render(
          chosen.map(cas =>
            (e: Energy, ma: Option[Energy]) =>
              !ec.validateFn(
                cas.flatMap(ca =>
                  (ca.currentChoices.flatMap(_.getEnergy.toList))
                ) :+ e
              )
          ),
          character
            .removeAbilityChoice(stagingKey, ability)
            .contramap(e => ec.withChoice(e)),
          character
            .addAbilityChoice(stagingKey, ability)
            .contramap(e => ec.withChoice(e))
        )
    )
  end renderEnergyChoices

  def renderActionChoices(
      character: CharacterModel,
      stagingKey: character.StagingKey,
      chosen: Signal[List[ChosenAbility]],
      ability: AbilityTemplate,
      ac: ActionChoice
  ): Element =
    span(
      onMouseOver --> { ev => ev.stopPropagation() },
      onClick --> { ev => ev.stopPropagation() },
      SelectWithPrevChoice[Action](
        Action.values.toList.filter(a => ac.validateFn(List(a))),
        a => a.toString
      )
        .render(
          chosen.map(cas =>
            (a: Action, ma: Option[Action]) =>
              !ac.validateFn(
                cas.flatMap(ca =>
                  (ca.currentChoices.flatMap(_.getAction.toList))
                ) :+ a
              )
          ),
          character
            .removeAbilityChoice(stagingKey, ability)
            .contramap(a => ac.withChoice(a)),
          character
            .addAbilityChoice(stagingKey, ability)
            .contramap(a => ac.withChoice(a))
        )
    )
  end renderActionChoices

  def renderPowerChoices(
      character: CharacterModel,
      stagingKey: character.StagingKey,
      powers: Signal[List[Power]],
      chosen: Signal[List[ChosenAbility]],
      ability: AbilityTemplate,
      pc: PowerChoice
  ): Element =
    span(
      onMouseOver --> { ev => ev.stopPropagation() },
      onClick --> { ev => ev.stopPropagation() },
      SelectWithPrevChoice
        .forSignal[Power](
          powers.map(_.filter(p => pc.validateFn(List(p)))),
          p => p.name
        )
        .render(
          chosen.map(cas =>
            { (p: Power, mp: Option[Power]) =>
              val chosenPowers = cas.flatMap(ca =>
                (ca.currentChoices.flatMap(_.getPower.toList))
              ) :+ p
              !pc.validateFn(chosenPowers)
            }
          ),
          character
            .removeAbilityChoice(stagingKey, ability)
            .contramap(p => pc.withChoice(p)),
          character
            .addAbilityChoice(stagingKey, ability)
            .contramap(p => pc.withChoice(p))
        )
    )
  end renderPowerChoices

  def renderQualityChoices(
      character: CharacterModel,
      stagingKey: character.StagingKey,
      qualities: Signal[List[Quality]],
      chosen: Signal[List[ChosenAbility]],
      ability: AbilityTemplate,
      qc: QualityChoice
  ): Element =
    span(
      onMouseOver --> { ev => ev.stopPropagation() },
      onClick --> { ev => ev.stopPropagation() },
      SelectWithPrevChoice
        .forSignal[Quality](
          qualities.map(_.filter(q => qc.validateFn(List(q)))),
          q => q.name
        )
        .render(
          chosen.map(cas =>
            (q: Quality, mq: Option[Quality]) =>
              !qc.validateFn(
                cas.flatMap(ca =>
                  (ca.currentChoices.flatMap(_.getQuality.toList))
                ) :+ q
              )
          ),
          character
            .removeAbilityChoice(stagingKey, ability)
            .contramap(q => qc.withChoice(q)),
          character
            .addAbilityChoice(stagingKey, ability)
            .contramap(q => qc.withChoice(q))
        )
    )
  end renderQualityChoices

  def renderPowerQualityChoices(
      character: CharacterModel,
      stagingKey: character.StagingKey,
      powers: Signal[List[Power]],
      qualities: Signal[List[Quality]],
      chosen: Signal[List[ChosenAbility]],
      ability: AbilityTemplate,
      pqc: PowerQualityChoice
  ): Element =
    span(
      onMouseOver --> { ev => ev.stopPropagation() },
      onClick --> { ev => ev.stopPropagation() },
      SelectWithPrevChoice
        .forSignal[Power | Quality](
          powers.combineWith(qualities).map((ps, qs) => ps ++ qs).map(_.filter(pq => pqc.validateFn(List(pq)))),
          pq => pq match
            case p: Power => p.name
            case q: Quality => q.name
        )
        .render(
          chosen.map(cas =>
            (pq: Power | Quality, mpq: Option[Power | Quality]) =>
              !pqc.validateFn(
                cas.flatMap(ca =>
                  (ca.currentChoices.flatMap(_.getPower.toList))
                ) :+ pq
              )
          ),
          character
            .removeAbilityChoice(stagingKey, ability)
            .contramap(pq => pqc.withChoice(pq)),
          character
            .addAbilityChoice(stagingKey, ability)
            .contramap(pq => pqc.withChoice(pq))
        )
    )
  end renderPowerQualityChoices
end RenderAbility
