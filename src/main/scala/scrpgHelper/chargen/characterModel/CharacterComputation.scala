package scrpgHelper.chargen.characterModel

import scrpgHelper.rolls.Die
import scrpgHelper.status.Status
import scrpgHelper.chargen.*
import monocle.{Lens, Optional, Traversal}
import monocle.syntax.all.*

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

  // Optics-based utility functions for character data analysis and transformation

  /** Get all qualities of a specific category using optics */
  def getQualitiesByCategory(data: CharacterData, category: QualityCategory): List[(Quality, Die)] =
    CharacterData.qualitiesByCategory(category).getAll(data)

  /** Get all powers of a specific category using optics */
  def getPowersByCategory(data: CharacterData, category: PowerCategory): List[(Power, Die)] =
    CharacterData.powersByCategory(category).getAll(data)

  /** Get all abilities with a specific status using optics */
  def getAbilitiesByStatus(data: CharacterData, status: Status): List[Ability[_]] =
    CharacterData.abilitiesByStatus(status).getAll(data)

  /** Get qualities for a specific staging key using optics */
  def getQualitiesForStaging(data: CharacterData, key: StagingKey): List[(Quality, Die)] =
    CharacterData.qualitiesForStaging(key).getAll(data)

  /** Get powers for a specific staging key using optics */
  def getPowersForStaging(data: CharacterData, key: StagingKey): List[(Power, Die)] =
    CharacterData.powersForStaging(key).getAll(data)

  /** Get abilities for a specific staging key using optics */
  def getAbilitiesForStaging(data: CharacterData, key: StagingKey): List[Ability[_]] =
    CharacterData.abilitiesForStaging(key).getAll(data)

  /** Apply die changes to all qualities using optics */
  def applyDieChangesToQualities(
      data: CharacterData,
      dieChanges: Map[Quality | Power, DieChange]
  ): List[(Quality, Die)] =
    CharacterData.allQualities.get(data).map { case (quality, die) =>
      val modifiedDie = dieChanges.get(quality).fold(die)(_.onDie(die))
      (quality, modifiedDie)
    }

  /** Apply die changes to all powers using optics */
  def applyDieChangesToPowers(
      data: CharacterData,
      dieChanges: Map[Quality | Power, DieChange]
  ): List[(Power, Die)] =
    CharacterData.allPowers.get(data).map { case (power, die) =>
      val modifiedDie = dieChanges.get(power).fold(die)(_.onDie(die))
      (power, modifiedDie)
    }

  /** Count qualities by category using optics */
  def countQualitiesByCategory(data: CharacterData): Map[QualityCategory, Int] =
    QualityCategory.values.map { category =>
      category -> CharacterData.qualitiesByCategory(category).length(data)
    }.toMap

  /** Count powers by category using optics */
  def countPowersByCategory(data: CharacterData): Map[PowerCategory, Int] =
    PowerCategory.values.map { category =>
      category -> CharacterData.powersByCategory(category).length(data)
    }.toMap

  /** Count abilities by status using optics */
  def countAbilitiesByStatus(data: CharacterData): Map[Status, Int] =
    Status.values.map { status =>
      status -> CharacterData.abilitiesByStatus(status).length(data)
    }.toMap

  /** Get the highest die value for a specific quality category */
  def getHighestDieForQualityCategory(data: CharacterData, category: QualityCategory): Option[Die] =
    CharacterData.qualitiesByCategory(category).getAll(data) match {
      case Nil => None
      case qualities => Some(qualities.map(_._2).maxBy(_.n))
    }

  /** Get the highest die value for a specific power category */
  def getHighestDieForPowerCategory(data: CharacterData, category: PowerCategory): Option[Die] =
    CharacterData.powersByCategory(category).getAll(data) match {
      case Nil => None
      case powers => Some(powers.map(_._2).maxBy(_.n))
    }

  /** Check if character has any abilities of a specific status */
  def hasAbilitiesWithStatus(data: CharacterData, status: Status): Boolean =
    CharacterData.abilitiesByStatus(status).nonEmpty(data)

  /** Validate staging requirements using optics */
  def validateStagingRequirements(data: CharacterData, key: StagingKey): Boolean =
    key match {
      case bg: Background =>
        val qualities = CharacterData.qualitiesForStaging(key).getAll(data)
        val abilities = CharacterData.abilitiesForStaging(key).getAll(data)
        bg.valid(qualities, abilities)
      case ps: PowerSource =>
        val powers = CharacterData.powersForStaging(key).getAll(data)
        val qualities = CharacterData.qualitiesForStaging(key).getAll(data).map(_._1)
        val abilities = CharacterData.abilitiesForStaging(key).getAll(data).collect { case ca: ChosenAbility => ca }
        // Get dice from background, not power source
        val dice = CharacterData.background.getOption(data).toList.flatMap(_.powerSourceDice)
        ps.valid(dice, powers, qualities, abilities)
      case at: Archetype =>
        val powers = CharacterData.powersForStaging(key).getAll(data).map(_._1)
        val qualities = CharacterData.qualitiesForStaging(key).getAll(data).map(_._1)
        val abilities = CharacterData.abilitiesForStaging(key).getAll(data).collect { case ca: ChosenAbility => ca }
        // Note: This is simplified - real validation would need access to other staging data
        true // Would need more complex validation logic
      case pt: Personality =>
        val qualities = CharacterData.qualitiesForStaging(key).getAll(data).map(_._1)
        val abilities = CharacterData.abilitiesForStaging(key).getAll(data).collect { case ca: ChosenAbility => ca }
        pt.valid(qualities, abilities)
      case _ => true
    }

end CharacterComputation