package scrpgHelper.chargen

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.components.SelectWithPrevChoice

object RenderAbility:
  import scrpgHelper.status.Status

  def renderPrincipleText(
      ability: Principle
  ): String =
    s"${ability.fullName} - ${ability.status.toString} - ${ability.principleCategory.toString} - ${ability.action.toString} - ${ability.description}"

  def renderChosenAbilityText(
      ability: ChosenAbility
  ): String =
    val actionText = ability.actions.map(_.toString).mkString(",")
    val descriptionText = renderChosenDescriptionText(ability)
    s"${ability.name} - ${ability.status.toString} - ${ability.category.toString} - ${actionText} - ${descriptionText}"
  end renderChosenAbilityText

  def renderChosenDescriptionText(
      ability: ChosenAbility
  ): String =
    ability.description.map { l =>
      l match
        case s: String         => s
        case ac: AbilityChoice => " " + ac.choiceName("<none>") + " "
    }.mkString

  def renderPrinciple(
      ability: Principle
  ): Element =
    div(
      className := s"ability status-${ability.status.toString.toLowerCase()}",
      span(
        className := "ability-actions",
        ability.action.toSymbol
      ),
      span(
        className := "ability-name",
        ability.fullName
      ),
      span(
        className := "ability-category",
        ability.category.toAbbreviation
      ),
      span(
        className := "ability-description",
        ability.description
      )
    )

  def renderChosenAbility(
      ability: ChosenAbility
  ): Element =
    div(
      className := s"ability status-${ability.status.toString.toLowerCase()}",
      span(
        className := "ability-actions",
        ability.actions.map(_.toSymbol).foldLeft("")(_ + _)
      ),
      span(
        className := "ability-name",
        ability.name
      ),
      span(
        className := "ability-category",
        ability.category.toAbbreviation
      ),
      span(
        className := "ability-description",
        renderChosenDescription(
          ability
        )
      )
    )
  end renderChosenAbility

  def renderChosenDescription(
      ability: ChosenAbility
  ): Element =
    span(
      ability.description.map { l =>
        l match
          case s: String => span(s)
          case ac: AbilityChoice =>
            renderChosenChoice(
              ability,
              ac
            )
      }
    )
  end renderChosenDescription

  def renderChosenChoice[Choice <: AbilityChoice](
      ability: ChosenAbility,
      choice: Choice
  ): Element =
    span(
      className := "ability-choice-chosen",
      choice.choiceName("<none>")
    )
  end renderChosenChoice

  def renderAbilityPool(
      character: CharacterModel,
      stagingKey: character.StagingKey,
      abilityPool: AbilityPool,
      showAbility: AbilityTemplate => Boolean
  ): Element =
    div(
      className := "ability-pool",
      span(s"Pick ${abilityPool.max}:"),
      div(
        abilityPool.abilities
          .filter(showAbility)
          .map(
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

  def renderAbilityPool(
      character: CharacterModel,
      stagingKey: character.StagingKey,
      abilityPool: AbilityPool
  ): Element =
    renderAbilityPool(character, stagingKey, abilityPool, _ => true)

  def renderAbility(
      character: CharacterModel,
      stagingKey: character.StagingKey,
      abilityPool: AbilityPool,
      template: AbilityTemplate,
      chosenSignal: Signal[Map[AbilityKey, ChosenAbility]]
  ): Element =
    val chosenAbility: Signal[Option[ChosenAbility]] =
      chosenSignal.map(_.get(template.key))
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
          if !l
              .collect { case ca: ChosenAbility => ca.template }
              .filter(at =>
                at.id == template.id && at.status != template.status
              )
              .isEmpty
          then "hidden"
          else ""
        ),
      span(
        className := "ability-actions",
        child.text <-- chosenAbility.map(
          _.fold("???")(_.actions.map(_.toSymbol).foldLeft("")(_ + _))
        )
      ),
      span(
        className := "ability-name",
        template.name
      ),
      span(
        className := "ability-category",
        template.category.toAbbreviation
      ),
      span(
        className := "ability-description",
        renderDescription(
          character,
          stagingKey,
          template,
          chosenSignal.map(
            _.values.toList.filter(_.inPool.id == abilityPool.id)
          ),
          character.abilitySelected(stagingKey, chosenAbility),
        )
      ),
      onMouseOver --> { _ => hovering.update { _ => true } },
      onMouseOut --> { _ => hovering.update { _ => false } },
      onBlur --> { _ => hovering.update { _ => false } },
      onClick.compose(_.withCurrentValueOf(chosenAbility)) --> {
        (ev, mChosen) =>
          mChosen.foreach { chosen =>
            character.toggleAbility(stagingKey).onNext(chosen)
          }
      }
    )
  end renderAbility

  def renderDescription(
      character: CharacterModel,
      stagingKey: character.StagingKey,
      ability: AbilityTemplate,
      chosen: Signal[List[ChosenAbility]],
      abilitySelected: Signal[Boolean]
  ): Element =
    val powerSignal: Signal[List[Power]] = stagingKey match
      case bg: Background => Signal.fromValue(List.empty)
      case ps: PowerSource =>
        character.powersSignal(Signal.fromValue(Some(ps))).map(_.map(_._1))
      case at: Archetype =>
        character
          .powersSignal(character.powerSourceSignal)
          .combineWith(character.powersSignal(Signal.fromValue(Some(at))))
          .map(_.map(_._1) ++ _.map(_._1))
      case p: Personality =>
        character
          .powersSignal(character.powerSourceSignal)
          .combineWith(character.powersSignal(character.archetypeSignal))
          .map(_.map(_._1) ++ _.map(_._1))
      case ra: RedAbility.RedAbilityPhase =>
        character
          .powersSignal(Signal.fromValue(Some(ra)))
          .combineWith(
            character.powersSignal(character.powerSourceSignal),
            character.powersSignal(character.archetypeSignal)
          )
          .map(_.map(_._1) ++ _.map(_._1) ++ _.map(_._1))

    val qualitySignal: Signal[List[Quality]] = stagingKey match
      case bg: Background =>
        character.qualitiesSignal(Signal.fromValue(Some(bg))).map(_.map(_._1))
      case ps: PowerSource =>
        character
          .qualitiesSignal(character.backgroundSignal)
          .combineWith(character.qualitiesSignal(Signal.fromValue(Some(ps))))
          .map(_.map(_._1) ++ _.map(_._1))
      case at: Archetype =>
        character
          .qualitiesSignal(character.backgroundSignal)
          .combineWith(
            character.qualitiesSignal(character.powerSourceSignal),
            character.qualitiesSignal(Signal.fromValue(Some(at)))
          )
          .map(_.map(_._1) ++ _.map(_._1) ++ _.map(_._1))
      case p: Personality =>
        character
          .qualitiesSignal(character.archetypeSignal)
          .combineWith(
            character.qualitiesSignal(character.powerSourceSignal),
            character.qualitiesSignal(character.backgroundSignal),
            character.qualitiesSignal(Signal.fromValue(Some(p)))
          )
          .map(_.map(_._1) ++ _.map(_._1) ++ _.map(_._1) ++ _.map(_._1))
      case ra: RedAbility.RedAbilityPhase =>
        character
          .qualitiesSignal(Signal.fromValue(Some(ra)))
          .combineWith(
            character.qualitiesSignal(character.backgroundSignal),
            character.qualitiesSignal(character.powerSourceSignal),
            character.qualitiesSignal(character.archetypeSignal),
            character.qualitiesSignal(character.personalitySignal)
          )
          .map(
            _.map(_._1) ++ _.map(_._1) ++ _.map(_._1) ++ _.map(_._1) ++ _.map(
              _._1
            )
          )

    val context: Signal[ChoiceContext] =
      powerSignal.combineWith(qualitySignal).map { (ps, qs) =>
        ChoiceContext(
          ps,
          qs,
          Action.values.toList,
          Energy.values.toList,
        )
      }

    span(
      ability.description.map { l =>
        l match
          case s: String => span(s)
          case ec: EnergyChoice =>
            renderChoices(
              character,
              stagingKey,
              ec,
              Signal.fromValue(Energy.values.toList),
              chosen,
              ability,
              _.getEnergy,
              abilitySelected,
              context,
            )
          case ac: ActionChoice =>
            renderChoices(
              character,
              stagingKey,
              ac,
              Signal.fromValue(Action.values.toList),
              chosen,
              ability,
              _.getAction,
              abilitySelected,
              context,
            )
          case pc: PowerChoice =>
            renderChoices(
              character,
              stagingKey,
              pc,
              powerSignal,
              chosen,
              ability,
              _.getPower,
              abilitySelected,
              context,
            )
          case qc: QualityChoice =>
            renderChoices(
              character,
              stagingKey,
              qc,
              qualitySignal,
              chosen,
              ability,
              _.getQuality,
              abilitySelected,
              context,
            )
          case pqc: PowerQualityChoice =>
            renderChoices(
              character,
              stagingKey,
              pqc,
              powerSignal.combineWith(qualitySignal).map { (ps, qs) =>
                ps ++ qs
              },
              chosen,
              ability,
              c => c.getPower.orElse(c.getQuality),
              abilitySelected,
              context,
            )
          case _: AbilityChoice => span("Not valid")
      }
    )
  end renderDescription

  def renderChoices[Choice <: AbilityChoice](
      character: CharacterModel,
      stagingKey: character.StagingKey,
      choice: Choice,
      items: Signal[List[choice.Item]],
      chosen: Signal[List[ChosenAbility]],
      ability: AbilityTemplate,
      retriever: AbilityChoice => Option[choice.Item],
      abilitySelected: Signal[Boolean],
      context: Signal[ChoiceContext],
  ): Element =
    span(
      onMouseOver.compose(_.withCurrentValueOf(abilitySelected)) --> { (ev, b) =>
        if b then ev.stopPropagation()
      },
      onClick.compose(_.withCurrentValueOf(abilitySelected)) --> { (ev, b) =>
        if b then ev.stopPropagation()
      },
      SelectWithPrevChoice
        .forSignal[choice.Item](
          items
            .combineWith(context)
            .map((is, ctx) => is.filter(i => choice.validateFn(List(i), ctx))),
          i => choice.itemName(i)
        )
        .render(
          chosen
            .combineWith(context)
            .map((cas, ctx) => { (p: choice.Item, mp: Option[choice.Item]) =>
            val chosenItems = cas.flatMap(ca =>
              (ca.currentChoices.flatMap(retriever(_).toList))
            ) :+ p
            !choice.validateFn(chosenItems, ctx)
          }),
          character
            .removeAbilityChoice(stagingKey, ability)
            .contramap(p => choice.withChoice(p)),
          character
            .addAbilityChoice(stagingKey, ability)
            .contramap(p => choice.withChoice(p))
        )
    )
  end renderChoices
end RenderAbility
