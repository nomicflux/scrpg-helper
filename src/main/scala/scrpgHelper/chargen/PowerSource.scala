package scrpgHelper.chargen

import scrpgHelper.rolls.Die
import scrpgHelper.rolls.EffectDieType
import scrpgHelper.status.Status

case class AbilityPool(abilities: List[Ability[_]])

object AbilityPool:
  def apply(abilities: Ability[_]*): AbilityPool =
    new AbilityPool(abilities.toList)
  end apply
end AbilityPool

case class PowerSource(
    name: String,
    number: Int,
    abilityChoices: List[AbilityPool],
    powerList: List[Power],
    powersDiePool: List[Die],
    archetypeDiePool: List[Die],
    chosenAbilities: List[ChosenAbility[_]]
):
  def valid(): Boolean = false

  def chooseAbility[A](ability: TextAbility[A], a: A)(using
      acA: AbilityChoice[A]
  ): Option[PowerSource] =
    if ability.runValidation(a, chosenAbilities)
    then Some(copy(chosenAbilities = chosenAbilities :+ ability.withChoice(a)))
    else None
  end chooseAbility
end PowerSource

object PowerSource:
  def statusValidation(
      max: Int,
      status: Status,
      abilities: List[Ability[_]]
  ): Boolean =
    abilities.filter(a => a.status == status).size <= max
  end statusValidation

  def apply(
      name: String,
      number: Int,
      abilityChoices: List[AbilityPool],
      powerList: List[Power],
      archetypeDiePool: List[Die]
  ): List[Die] => PowerSource =
    ds =>
      new PowerSource(
        name,
        number,
        abilityChoices,
        powerList,
        ds,
        archetypeDiePool,
        List()
      )
  end apply

end PowerSource
