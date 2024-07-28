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
    extraPower: List[Die] => Option[(Die, List[Power])],
    upgrades: Option[((Quality | Power, Die) => Boolean)],
    downgrades: Option[((Quality | Power, Die) => Boolean)],
):
  def valid(
      diePool: List[Die],
      powers: List[(Power, Die)],
      qualities: List[Quality],
      abilities: List[ChosenAbility]
  ): Boolean =
    abilities.filter(_.valid).size == abilityPools.map(_.max).sum &&
      powers.size == (diePool.size + extraPower(diePool).fold(0)(_ => 1)) &&
      qualities.size == extraQuality.fold(0)(_ => 1)
  end valid

  def withConditionalExtraPowers(fn: List[Die] => Option[(Die, List[Power])]): PowerSource =
    copy(extraPower = fn)

  def withUpgrades(fn: (Quality | Power, Die) => Boolean): PowerSource =
    copy(upgrades = Some(fn))

  def withDowngrades(fn: (Quality | Power, Die) => Boolean): PowerSource =
    copy(downgrades = Some(fn))
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
    powerList.distinct,
    archetypeDiePool,
    None,
    _ => None,
    None,
    None,
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
    (mandatoryPowers ++ powerList).distinct,
    archetypeDiePool,
    None,
    _ => None,
    None,
    None,
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
    powerList.distinct,
    archetypeDiePool,
    extraQuality,
    _ => extraPower,
    None,
    None,
  )

  def uniquePowers(abilities: List[ChosenAbility]): Boolean =
    val powers: List[Power] =
      abilities.flatMap(_.currentChoices).flatMap(_.getPower.toList)
    powers.distinct == powers
  end uniquePowers

  val powerSources: List[PowerSource] = List(
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
    Alien.alien,
    Genius.genius,
    Cosmos.cosmos,
    Extradimensional.extradimensional,
    Unknown.unknown,
    HigherPower.higherPower,
    Multiverse.multiverse,
  )
end PowerSource
