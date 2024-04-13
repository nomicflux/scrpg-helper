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
    archetypeDiePool: List[Die],
    extraQuality: Option[(Die, List[Quality])],
    extraPower: Option[(Die, List[Power])]
):
  def valid(
      diePool: List[Die],
      powers: List[Power],
      qualities: List[Quality],
      abilities: List[ChosenAbility]
  ): Boolean =
    abilities.filter(_.valid).size == abilityPools.map(_.max).sum &&
      powers.size == (diePool.size + extraPower.fold(0)(_ => 1)) &&
      qualities.size == extraQuality.fold(0)(_ => 1)
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
    archetypeDiePool,
    None,
    None
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
    archetypeDiePool,
    None,
    None
  )

  def apply(
      name: String,
      number: Int,
      abilityPools: List[AbilityPool],
      powerList: List[Power],
      archetypeDiePool: List[Die],
      extraQuality: Option[(Die, List[Quality])],
      extraPower: Option[(Die, List[Power])]
  ): PowerSource = new PowerSource(
    name,
    number,
    abilityPools,
    List(),
    powerList,
    archetypeDiePool,
    extraQuality,
    extraPower
  )

  def uniquePowers(abilities: List[ChosenAbility]): Boolean =
    val powers: List[Power] =
      abilities.flatMap(_.currentChoices).flatMap(_.getPower.toList)
    powers.distinct == powers
  end uniquePowers

  def powerSources: List[PowerSource] = List(
    Accident.accident,
    // Training.training, // TODO: Implement adding quality from archetype in next step
    Genetic.genetic,
    Experimentation.experimentation,
    Mystical.mystical,
    Nature.nature,
    Relic.relic,
    PoweredSuit.poweredSuit,
    Radiation.radiation,
    TechUpgrades.techUpgrades,
    Supernatural.supernatural,
    ArtificialBeing.artificialBeing,
    Cursed.cursed,
    // Alien.alien, // TODO: Implement upgrade or add
    Genius.genius,
    // Cosmos.cosmos, // TODO: Implement downgrade and upgrade
    Extradimensional.extradimensional,
    Unknown.unknown,
    HigherPower.higherPower,
    Multiverse.multiverse,
  )
end PowerSource
