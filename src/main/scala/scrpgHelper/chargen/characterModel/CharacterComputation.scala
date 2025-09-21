package scrpgHelper.chargen.characterModel

import scrpgHelper.rolls.Die
import scrpgHelper.status.Status
import scrpgHelper.chargen.*

/** Character computation logic - pure functions for computing character data.
  *
  * This class contains all the business logic for computing derived character values
  * from basic character state and staging data. All methods are pure functions that
  * take explicit parameters and return computed results without side effects.
  */
object CharacterComputation:

  type StagingKey = Background | PowerSource | Archetype | Personality | RedAbility.RedAbilityPhase

  enum DieChange:
    case Upgrade, Downgrade

    def onDie(d: Die): Die = this match
      case Upgrade   => d.upgrade
      case Downgrade => d.downgrade
  end DieChange

  object DieChange:
    def combine(
        ths: Option[DieChange],
        that: Option[DieChange]
    ): Option[DieChange] =
      (ths, that) match
        case (None, None)       => None
        case (Some(x), None)    => Some(x)
        case (None, Some(y))    => Some(y)
        case (Some(x), Some(y)) => if x == y then Some(x) else None
  end DieChange

  def computeAllQualities(
      qualityStaging: Map[StagingKey, List[(Quality, Die)]],
      background: Option[Background],
      powerSource: Option[PowerSource],
      archetype: Option[Archetype],
      personality: Option[Personality],
      dieChanges: Map[StagingKey, Map[Quality | Power, DieChange]],
      allDieChangesFunction: (
          Map[StagingKey, Map[Quality | Power, DieChange]],
          Option[PowerSource],
          Option[Archetype],
          Option[Personality]
      ) => Map[Quality | Power, DieChange]
  ): List[(Quality, Die)] =
    val relDieChanges = allDieChangesFunction(dieChanges, powerSource, archetype, personality)
    val baseQualities = background.fold(List())(bg => qualityStaging.getOrElse(bg, List())) ++
      powerSource.fold(List())(ps => qualityStaging.getOrElse(ps, List())) ++
      archetype.fold(List())(at => qualityStaging.getOrElse(at, List())) ++
      personality.fold(List())(pt => qualityStaging.getOrElse(pt, List()))
    baseQualities.map { case (q, d) =>
      (q, relDieChanges.get(q).fold(d)(dc => dc.onDie(d)))
    }

  def computeAllPowers(
      powerStaging: Map[StagingKey, List[(Power, Die)]],
      background: Option[Background],
      powerSource: Option[PowerSource],
      archetype: Option[Archetype],
      personality: Option[Personality],
      dieChanges: Map[StagingKey, Map[Quality | Power, DieChange]],
      allDieChangesFunction: (
          Map[StagingKey, Map[Quality | Power, DieChange]],
          Option[PowerSource],
          Option[Archetype],
          Option[Personality]
      ) => Map[Quality | Power, DieChange]
  ): List[(Power, Die)] =
    val relDieChanges = allDieChangesFunction(dieChanges, powerSource, archetype, personality)
    val basePowers = background.fold(List())(bg => powerStaging.getOrElse(bg, List())) ++
      powerSource.fold(List())(ps => powerStaging.getOrElse(ps, List())) ++
      archetype.fold(List())(at => powerStaging.getOrElse(at, List())) ++
      personality.fold(List())(pt => powerStaging.getOrElse(pt, List()))
    basePowers.map { case (p, d) =>
      (p, relDieChanges.get(p).fold(d)(dc => dc.onDie(d)))
    }

  def computeAllStagedAbilities(
      abilityStaging: Map[StagingKey, List[Ability[_]]],
      background: Option[Background],
      powerSource: Option[PowerSource],
      archetype: Option[Archetype],
      personality: Option[Personality]
  ): List[ChosenAbility] =
    val result = background.fold(List())(bg => abilityStaging.getOrElse(bg, List())) ++
      powerSource.fold(List())(ps => abilityStaging.getOrElse(ps, List())) ++
      archetype.fold(List())(at => abilityStaging.getOrElse(at, List())) ++
      personality.fold(List())(pt => abilityStaging.getOrElse(pt, List())) ++
      abilityStaging.getOrElse(RedAbility.redAbilityPhase, List())
    result.collect { case ca: ChosenAbility => ca }

  def computeAllChosenAbilities(
      abilityChoice: Map[StagingKey, Map[AbilityKey, ChosenAbility]],
      background: Option[Background],
      powerSource: Option[PowerSource],
      archetype: Option[Archetype],
      personality: Option[Personality]
  ): List[ChosenAbility] =
    val result = background.fold(List())(bg => abilityChoice.getOrElse(bg, List())) ++
      powerSource.fold(List())(ps => abilityChoice.getOrElse(ps, List())) ++
      archetype.fold(List())(at => abilityChoice.getOrElse(at, List())) ++
      personality.fold(List())(pt => abilityChoice.getOrElse(pt, List())) ++
      abilityChoice.getOrElse(RedAbility.redAbilityPhase, List())
    result.map(_._2).filter(_.descriptionFilledOut).toList

  def computeAllPrinciples(
      abilityStaging: Map[StagingKey, List[Ability[_]]],
      background: Option[Background],
      archetype: Option[Archetype]
  ): List[Principle] =
    val result = background.fold(List())(bg => abilityStaging.getOrElse(bg, List())) ++
      archetype.fold(List())(at => abilityStaging.getOrElse(at, List()))
    result.collect { case p: Principle => p }

  def computeAllAbilities(
      allStagedAbilities: List[ChosenAbility],
      allChosenAbilities: List[ChosenAbility],
      allPrinciples: List[Principle]
  ): List[Ability[_]] =
    val abilityIds = allStagedAbilities.map(_.key).toSet
    allChosenAbilities.filter(a => abilityIds.contains(a.key)) ++ allPrinciples

  def computeRedZoneHealth(personality: Option[Personality]): Option[Int] =
    personality.flatMap(p => p.statusDice.get(Status.Red).map(_.n))

  def computePowerQualityHealth(
      allPowers: List[(Power, Die)],
      allQualities: List[(Quality, Die)],
      archetype: Option[Archetype],
      personality: Option[Personality]
  ): Int =
    val acceptableCats: Set[QualityCategory | PowerCategory] =
      Set(PowerCategory.Athletic, QualityCategory.Mental)
        .union(archetype.fold(Set())(_.extraHealthCategories.toSet))
        .union(personality.fold(Set()) { pe =>
          (PowerCategory.values ++ QualityCategory.values)
            .filter((cat: QualityCategory | PowerCategory) =>
              pe.extraHealthCheck(cat)
            )
            .toSet
        })
    val powerRolls =
      allPowers.filter((pd: (Power, Die)) =>
        acceptableCats.contains(pd._1.category)
      ).map(_._2.n)
    val maxPower = if powerRolls.isEmpty then 4 else powerRolls.max
    val qualityRolls =
      allQualities.filter((qd: (Quality, Die)) =>
        acceptableCats.contains(qd._1.category)
      ).map(_._2.n)
    val maxQuality = if qualityRolls.isEmpty then 4 else qualityRolls.max
    List(maxPower, maxQuality).max

end CharacterComputation