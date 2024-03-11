package scrpgHelper.chargen

import scrpgHelper.rolls.EffectDieType
import scrpgHelper.status.Status

type Description = String

enum AbilityCategory:
  case Action, Reaction, Inherent
end AbilityCategory

enum Target:
  case Self, Ally, Opponent, Any, Same, Other
  case Multiple(target: List[Target])
end Target

case class TextAbility[A: AbilityChoice](
    name: String,
    status: Status,
    category: AbilityCategory,
    actions: A => List[Action],
    description: A => Description,
    validate: (A, ChosenAbility[A], List[ChosenAbility[_]]) => Boolean
) extends Ability[TextAbility[A]]:
  def changeName(s: String): TextAbility[A] = copy(name = s)

  def withChoice(a: A): ChosenAbility[A] =
    ChosenAbility[A](name, status, category, actions(a), description(a), a)

  def runValidation(a: A, abilities: List[ChosenAbility[_]]): Boolean =
    validate(a, withChoice(a), abilities)
end TextAbility

case class ChosenAbility[A: AbilityChoice](
    name: String,
    status: Status,
    category: AbilityCategory,
    actions: List[Action],
    description: Description,
    choice: A
) extends Ability[ChosenAbility[A]]:
  def changeName(s: String): ChosenAbility[A] =
    copy(name = s)
end ChosenAbility

trait Ability[A]:
  def status: Status
  def name: String
  def category: AbilityCategory
  def changeName(s: String): A

  def inPrincipleCategory(category: PrincipleCategory): Boolean = false
end Ability
