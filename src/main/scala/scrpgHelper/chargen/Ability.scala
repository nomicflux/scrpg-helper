package scrpgHelper.chargen

import scrpgHelper.rolls.EffectDieType
import scrpgHelper.status.Status

type Description = String

enum AbilityCategory:
  case Action, Reaction, Inherent

  def toAbbreviation: String = this match
    case Action => "A"
    case Reaction => "R"
    case Inherent => "I"
end AbilityCategory

case class AbilityTemplate(
    name: String,
    status: Status,
    category: AbilityCategory,
    actions: List[AbilityChoice] => List[Action],
    description: List[AbilityChoice] => Description,
    baseChoices: List[AbilityChoice]
) extends Ability[AbilityTemplate]:
  def changeName(s: String): AbilityTemplate = copy(name = s)

  def withChoices(as: List[AbilityChoice]): ChosenAbility =
    ChosenAbility(this, as)
end AbilityTemplate

case class ChosenAbility(
  template: AbilityTemplate,
  currentChoices: List[AbilityChoice]
) extends Ability[ChosenAbility]:
  def status: Status = template.status
  def name: String = template.name
  def category: AbilityCategory = template.category
  def changeName(s: String): ChosenAbility =
    copy(template = template.changeName(s))
end ChosenAbility

trait Ability[A]:
  def status: Status
  def name: String
  def category: AbilityCategory
  def changeName(s: String): A

  def inPrincipleCategory(category: PrincipleCategory): Boolean = false
end Ability
