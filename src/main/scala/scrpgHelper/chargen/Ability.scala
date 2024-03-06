package scrpgHelper.chargen

import scrpgHelper.rolls.EffectDieType
import scrpgHelper.status.Status

enum AbilityCategory:
  case Action, Reaction, Inherent
end AbilityCategory

enum Target:
  case Self, Ally, Opponent
  case Multiple(target: List[Target])
end Target

trait Ability[A]:
  val status: Status
  val name: String
  val category: AbilityCategory
  def changeName(s: String): A

  def inPrincipleCategory(category: PrincipleCategory): Boolean = false
end Ability

case class SimpleAbility(
    name: String,
    action: Action,
    status: Status,
    diePool: Set[EffectDieType],
    powerQuality: Power | Quality,
    category: AbilityCategory,
    targets: List[Target]
) extends Ability[SimpleAbility]:
  def changeName(s: String): SimpleAbility =
    copy(name = s)
  end changeName
end SimpleAbility

object SimpleAbility:
  def apply(
      name: String,
      action: Action,
      status: Status,
      die: EffectDieType,
      powerQuality: Power | Quality,
      category: AbilityCategory,
      target: Target
  ): SimpleAbility =
    new SimpleAbility(
      name,
      action,
      status,
      Set(die),
      powerQuality,
      category,
      List(target)
    )
  end apply
end SimpleAbility
