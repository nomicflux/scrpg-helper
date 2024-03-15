package scrpgHelper.chargen

import scrpgHelper.rolls.Die
import scrpgHelper.rolls.EffectDieType
import scrpgHelper.status.Status

case class PowerSourceWithChoices(powerSource: PowerSource,
                                  dicePool: List[Die],
                                  choices: List[AbilityChoice])

case class PowerSource(
    name: String,
    number: Int,
    abilityPools: List[AbilityPool],
    powerList: List[Power],
    archetypeDiePool: List[Die],
):
  def valid(): Boolean = false
end PowerSource

object PowerSource:
  import scrpgHelper.chargen.powers.*

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
      abilityPools: List[AbilityPool],
      powerList: List[Power],
      archetypeDiePool: List[Die]
  ): PowerSource =
      new PowerSource(
        name,
        number,
        abilityPools,
        powerList,
        archetypeDiePool,
      )
  end apply

  def uniquePowers(abilities: List[ChosenAbility]): Boolean =
    val powers: List[Power] = abilities.flatMap(_.currentChoices).flatMap(_.getPower.toList)
    powers.distinct == powers
  end uniquePowers

  def powerSources: List[PowerSource] = List(
    Accident.accident,
  )
end PowerSource
