package scrpgHelper.chargen

import scrpgHelper.rolls.EffectDieType
import scrpgHelper.status.Status

type Description = List[String | AbilityChoice]

enum AbilityCategory:
  case Action, Reaction, Inherent

  def toAbbreviation: String = this match
    case Action => "A"
    case Reaction => "R"
    case Inherent => "I"
end AbilityCategory

case class AbilityTemplate(
  id: AbilityId,
    name: String,
    status: Status,
    category: AbilityCategory,
    actions: List[AbilityChoice] => List[Action],
    description: Description
) extends Ability[AbilityTemplate]:
  val baseChoices: List[AbilityChoice] = description.collect { case ac: AbilityChoice => ac }

  def changeName(s: String): AbilityTemplate = copy(name = s)

  def toChosenAbility(pool: AbilityPool): ChosenAbility =
    ChosenAbility(this, pool, baseChoices)

  def withChoices(pool: AbilityPool, as: List[AbilityChoice]): ChosenAbility =
    ChosenAbility(this, pool, as)
end AbilityTemplate

object AbilityTemplate:
    def apply(
    name: String,
    status: Status,
    category: AbilityCategory,
    actions: List[AbilityChoice] => List[Action],
    description: Description
    ): AbilityTemplate =
      new AbilityTemplate(
        new AbilityId(),
        name,
        status,
        category,
        actions,
        description
      )
end AbilityTemplate

case class ChosenAbility(
  template: AbilityTemplate,
  inPool: AbilityPool,
  currentChoices: List[AbilityChoice]
) extends Ability[ChosenAbility]:
  val id: AbilityId = template.id
  val status: Status = template.status
  val name: String = template.name
  val category: AbilityCategory = template.category
  def changeName(s: String): ChosenAbility =
    copy(template = template.changeName(s))

  override def toString(): String =
    s"ChosenAbility(${template.name}, ${template.status}, ${template.category}, ${inPool.max}/${inPool.abilities.size}, ${currentChoices.map(_.toString)} Valid: ${valid}})"

  def valid: Boolean =
    AbilityChoice.numChosen(currentChoices) == template.baseChoices.size
  end valid

  def actions: List[Action] = template.actions(currentChoices)
  def description: Description = template.description.map {
    case s: String => s
    case ac: AbilityChoice => currentChoices.find(_.id == ac.id).getOrElse(ac)
  }

  def applyChoice(choice: AbilityChoice): ChosenAbility =
    copy(currentChoices = currentChoices.map(c => if c.id == choice.id then choice else c))
  end applyChoice

  def removeChoice(choice: AbilityChoice): ChosenAbility =
    copy(currentChoices = currentChoices.map(c => if c.id == choice.id then choice.withoutChoice else c))
  end removeChoice

  override def equals(x: Any): Boolean = x match
    case ca: ChosenAbility => ca.template == this.template
    case _ => false
end ChosenAbility

class AbilityId

trait Ability[A]:
  val id: AbilityId
  val status: Status
  val name: String
  val category: AbilityCategory
  def changeName(s: String): A

  def inPrincipleCategory(category: PrincipleCategory): Boolean = false
end Ability
