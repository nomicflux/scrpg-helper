package scrpgHelper.chargen

import scrpgHelper.rolls.Die
import scrpgHelper.rolls.EffectDieType
import scrpgHelper.status.Status

case class PowerSource(
    name: String,
    number: Int,
    abilityPools: List[AbilityPool],
    mandatoryPowers: List[Power],
    powerList: List[Power],
    archetypeDiePool: List[Die]
):
  def valid(
      diePool: List[Die],
      powers: List[Power],
      abilities: List[ChosenAbility]
  ): Boolean =
    abilities.filter(_.valid).size == abilityPools.map(_.max).sum &&
      powers.size == diePool.size
  end valid
end PowerSource

object PowerSource:
  import scrpgHelper.chargen.powers.*

  def statusValidation(
      max: Int,
      status: Status,
      abilities: List[Ability[_]]
  ): Boolean = abilities.filter(a => a.status == status).size <= max

  def apply(
      name: String,
      number: Int,
      abilityPools: List[AbilityPool],
      powerList: List[Power],
      archetypeDiePool: List[Die]
  ): PowerSource = apply(
    name,
    number,
    abilityPools,
    List(),
    powerList,
    archetypeDiePool
  )

  def apply(
      name: String,
      number: Int,
      abilityPools: List[AbilityPool],
      mandatoryPowers: List[Power],
      powerList: List[Power],
      archetypeDiePool: List[Die]
  ): PowerSource = new PowerSource(
    name,
    number,
    abilityPools,
    mandatoryPowers,
    powerList,
    archetypeDiePool
  )

  def uniquePowers(abilities: List[ChosenAbility]): Boolean =
    val powers: List[Power] =
      abilities.flatMap(_.currentChoices).flatMap(_.getPower.toList)
    powers.distinct == powers
  end uniquePowers

  // TODO: There is another powersource here with mandatory powers
  def powerSources: List[PowerSource] = List(
    Accident.accident,
    Genetic.genetic,
    Experimentation.experimentation,
    Nature.nature,
    Relic.relic,
    PoweredSuit.poweredSuit,
    Radiation.radiation,
    TechUpgrades.techUpgrades,
    ArtificialBeing.artificialBeing,
    Cursed.cursed,
    Extradimensional.extradimensional,
    HigherPower.higherPower
  )
end PowerSource
