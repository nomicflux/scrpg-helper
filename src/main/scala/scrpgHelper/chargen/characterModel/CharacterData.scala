package scrpgHelper.chargen.characterModel

import scrpgHelper.rolls.Die
import scrpgHelper.status.Status
import scrpgHelper.chargen.*
import monocle.{Lens, Optional, Traversal}
import monocle.syntax.all.*

/** Character data interface - pure data access without reactive framework.
  *
  * This trait provides pure functions for accessing character state and computed values.
  * All methods return raw data without Signals or reactive behavior, making it ideal
  * for testing, computation, and scenarios where synchronous data access is needed.
  */
trait CharacterData:

  // Type definitions from computation object
  type StagingKey = CharacterComputation.StagingKey
  type DieChange = CharacterComputation.DieChange

  // Core character selections - raw values
  def background: Option[Background]
  def powerSource: Option[PowerSource]
  def archetype: Option[Archetype]
  def personality: Option[Personality]
  def health: Option[Int]

  // Computed values - pure functions
  def allQualities: List[(Quality, Die)]
  def allPowers: List[(Power, Die)]
  def allStagedAbilities: List[ChosenAbility]
  def allChosenAbilities: List[ChosenAbility]
  def allPrinciples: List[Principle]
  def allAbilities: List[Ability[_]]

  // Health calculations - pure functions
  def redZoneHealth: Option[Int]
  def powerQualityHealth: Int

  // Staging data access - pure functions (needed for validation)
  def qualityStaging: Map[StagingKey, List[(Quality, Die)]]
  def powerStaging: Map[StagingKey, List[(Power, Die)]]
  def abilityStaging: Map[StagingKey, List[Ability[_]]]
  def abilityChoice: Map[StagingKey, Map[AbilityKey, ChosenAbility]]
  def dieChanges: Map[StagingKey, Map[Quality | Power, DieChange]]
  def baseAbilities: Map[StagingKey, Map[AbilityKey, ChosenAbility]]

end CharacterData

/** Optics for CharacterData - provides lenses and optionals for data access and transformation.
  *
  * These optics enable functional updates and focused access to character data.
  * Particularly useful for validation, data transformation, and nested updates.
  */
object CharacterData:

  // Core selections - Optional lenses since these are Option types
  def background: Optional[CharacterData, Background] =
    Optional[CharacterData, Background](_.background)(bg => data =>
      new CharacterData {
        def background = Some(bg)
        def powerSource = data.powerSource
        def archetype = data.archetype
        def personality = data.personality
        def health = data.health
        def allQualities = data.allQualities
        def allPowers = data.allPowers
        def allStagedAbilities = data.allStagedAbilities
        def allChosenAbilities = data.allChosenAbilities
        def allPrinciples = data.allPrinciples
        def allAbilities = data.allAbilities
        def redZoneHealth = data.redZoneHealth
        def powerQualityHealth = data.powerQualityHealth
        def qualityStaging = data.qualityStaging
        def powerStaging = data.powerStaging
        def abilityStaging = data.abilityStaging
        def abilityChoice = data.abilityChoice
        def dieChanges = data.dieChanges
        def baseAbilities = data.baseAbilities
      }
    )

  def powerSource: Optional[CharacterData, PowerSource] =
    Optional[CharacterData, PowerSource](_.powerSource)(ps => data =>
      new CharacterData {
        def background = data.background
        def powerSource = Some(ps)
        def archetype = data.archetype
        def personality = data.personality
        def health = data.health
        def allQualities = data.allQualities
        def allPowers = data.allPowers
        def allStagedAbilities = data.allStagedAbilities
        def allChosenAbilities = data.allChosenAbilities
        def allPrinciples = data.allPrinciples
        def allAbilities = data.allAbilities
        def redZoneHealth = data.redZoneHealth
        def powerQualityHealth = data.powerQualityHealth
        def qualityStaging = data.qualityStaging
        def powerStaging = data.powerStaging
        def abilityStaging = data.abilityStaging
        def abilityChoice = data.abilityChoice
        def dieChanges = data.dieChanges
        def baseAbilities = data.baseAbilities
      }
    )

  def archetype: Optional[CharacterData, Archetype] =
    Optional[CharacterData, Archetype](_.archetype)(at => data =>
      new CharacterData {
        def background = data.background
        def powerSource = data.powerSource
        def archetype = Some(at)
        def personality = data.personality
        def health = data.health
        def allQualities = data.allQualities
        def allPowers = data.allPowers
        def allStagedAbilities = data.allStagedAbilities
        def allChosenAbilities = data.allChosenAbilities
        def allPrinciples = data.allPrinciples
        def allAbilities = data.allAbilities
        def redZoneHealth = data.redZoneHealth
        def powerQualityHealth = data.powerQualityHealth
        def qualityStaging = data.qualityStaging
        def powerStaging = data.powerStaging
        def abilityStaging = data.abilityStaging
        def abilityChoice = data.abilityChoice
        def dieChanges = data.dieChanges
        def baseAbilities = data.baseAbilities
      }
    )

  def personality: Optional[CharacterData, Personality] =
    Optional[CharacterData, Personality](_.personality)(pt => data =>
      new CharacterData {
        def background = data.background
        def powerSource = data.powerSource
        def archetype = data.archetype
        def personality = Some(pt)
        def health = data.health
        def allQualities = data.allQualities
        def allPowers = data.allPowers
        def allStagedAbilities = data.allStagedAbilities
        def allChosenAbilities = data.allChosenAbilities
        def allPrinciples = data.allPrinciples
        def allAbilities = data.allAbilities
        def redZoneHealth = data.redZoneHealth
        def powerQualityHealth = data.powerQualityHealth
        def qualityStaging = data.qualityStaging
        def powerStaging = data.powerStaging
        def abilityStaging = data.abilityStaging
        def abilityChoice = data.abilityChoice
        def dieChanges = data.dieChanges
        def baseAbilities = data.baseAbilities
      }
    )

  def health: Optional[CharacterData, Int] =
    Optional[CharacterData, Int](_.health)(h => data =>
      new CharacterData {
        def background = data.background
        def powerSource = data.powerSource
        def archetype = data.archetype
        def personality = data.personality
        def health = Some(h)
        def allQualities = data.allQualities
        def allPowers = data.allPowers
        def allStagedAbilities = data.allStagedAbilities
        def allChosenAbilities = data.allChosenAbilities
        def allPrinciples = data.allPrinciples
        def allAbilities = data.allAbilities
        def redZoneHealth = data.redZoneHealth
        def powerQualityHealth = data.powerQualityHealth
        def qualityStaging = data.qualityStaging
        def powerStaging = data.powerStaging
        def abilityStaging = data.abilityStaging
        def abilityChoice = data.abilityChoice
        def dieChanges = data.dieChanges
        def baseAbilities = data.baseAbilities
      }
    )

  // Computed data views - these are read-only lenses since they're computed
  def allQualities: Lens[CharacterData, List[(Quality, Die)]] =
    Lens[CharacterData, List[(Quality, Die)]](_.allQualities)(_ => identity)

  def allPowers: Lens[CharacterData, List[(Power, Die)]] =
    Lens[CharacterData, List[(Power, Die)]](_.allPowers)(_ => identity)

  def allAbilities: Lens[CharacterData, List[Ability[_]]] =
    Lens[CharacterData, List[Ability[_]]](_.allAbilities)(_ => identity)

  // Staging data views - these provide access to the underlying maps
  def qualityStaging: Lens[CharacterData, Map[CharacterData#StagingKey, List[(Quality, Die)]]] =
    Lens[CharacterData, Map[CharacterData#StagingKey, List[(Quality, Die)]]](_.qualityStaging)(_ => identity)

  def powerStaging: Lens[CharacterData, Map[CharacterData#StagingKey, List[(Power, Die)]]] =
    Lens[CharacterData, Map[CharacterData#StagingKey, List[(Power, Die)]]](_.powerStaging)(_ => identity)

  def abilityStaging: Lens[CharacterData, Map[CharacterData#StagingKey, List[Ability[_]]]] =
    Lens[CharacterData, Map[CharacterData#StagingKey, List[Ability[_]]]](_.abilityStaging)(_ => identity)

  // Traversals for working with collections
  def qualitiesForStaging(key: CharacterData#StagingKey): Traversal[CharacterData, (Quality, Die)] =
    qualityStaging.andThen(
      Lens[Map[CharacterData#StagingKey, List[(Quality, Die)]], List[(Quality, Die)]](
        _.getOrElse(key, List())
      )(list => map => map + (key -> list))
    ).andThen(Traversal.fromTraverse[List, (Quality, Die)])

  def powersForStaging(key: CharacterData#StagingKey): Traversal[CharacterData, (Power, Die)] =
    powerStaging.andThen(
      Lens[Map[CharacterData#StagingKey, List[(Power, Die)]], List[(Power, Die)]](
        _.getOrElse(key, List())
      )(list => map => map + (key -> list))
    ).andThen(Traversal.fromTraverse[List, (Power, Die)])

  def abilitiesForStaging(key: CharacterData#StagingKey): Traversal[CharacterData, Ability[_]] =
    abilityStaging.andThen(
      Lens[Map[CharacterData#StagingKey, List[Ability[_]]], List[Ability[_]]](
        _.getOrElse(key, List())
      )(list => map => map + (key -> list))
    ).andThen(Traversal.fromTraverse[List, Ability[_]])

  // Filters and focused views
  def qualitiesByCategory(category: QualityCategory): Traversal[CharacterData, (Quality, Die)] =
    allQualities.andThen(Traversal.fromTraverse[List, (Quality, Die)].filter(_._1.category == category))

  def powersByCategory(category: PowerCategory): Traversal[CharacterData, (Power, Die)] =
    allPowers.andThen(Traversal.fromTraverse[List, (Power, Die)].filter(_._1.category == category))

  def abilitiesByStatus(status: Status): Traversal[CharacterData, Ability[_]] =
    allAbilities.andThen(Traversal.fromTraverse[List, Ability[_]].filter(_.status == status))

  // Efficient focused optics for signal composition

  /** Lens to extract qualities from a specific staging key with die changes applied */
  def qualitiesFromStaging(stagingKey: CharacterComputation.StagingKey, dieChanges: Map[Quality | Power, CharacterComputation.DieChange]): Lens[Map[CharacterComputation.StagingKey, List[(Quality, Die)]], List[(Quality, Die)]] =
    Lens[Map[CharacterComputation.StagingKey, List[(Quality, Die)]], List[(Quality, Die)]](
      _.getOrElse(stagingKey, List())
    )(modifiedQualities => map => map + (stagingKey -> modifiedQualities))
      .andThen(Lens[List[(Quality, Die)], List[(Quality, Die)]](
        _.map { case (q, d) => (q, dieChanges.get(q).fold(d)(dc => dc.onDie(d))) }
      )(newQualities => _ => newQualities))

  /** Lens to extract powers from a specific staging key with die changes applied */
  def powersFromStaging(stagingKey: CharacterComputation.StagingKey, dieChanges: Map[Quality | Power, CharacterComputation.DieChange]): Lens[Map[CharacterComputation.StagingKey, List[(Power, Die)]], List[(Power, Die)]] =
    Lens[Map[CharacterComputation.StagingKey, List[(Power, Die)]], List[(Power, Die)]](
      _.getOrElse(stagingKey, List())
    )(modifiedPowers => map => map + (stagingKey -> modifiedPowers))
      .andThen(Lens[List[(Power, Die)], List[(Power, Die)]](
        _.map { case (p, d) => (p, dieChanges.get(p).fold(d)(dc => dc.onDie(d))) }
      )(newPowers => _ => newPowers))

  /** Lens to extract chosen abilities from a specific staging key */
  def chosenAbilitiesFromStaging(stagingKey: CharacterComputation.StagingKey): Lens[Map[CharacterComputation.StagingKey, List[Ability[_]]], List[ChosenAbility]] =
    Lens[Map[CharacterComputation.StagingKey, List[Ability[_]]], List[ChosenAbility]](
      _.getOrElse(stagingKey, List()).collect { case ca: ChosenAbility => ca }
    )(abilities => map => map + (stagingKey -> abilities))

  /** Lens to extract principles from a specific staging key */
  def principlesFromStaging(stagingKey: CharacterComputation.StagingKey): Lens[Map[CharacterComputation.StagingKey, List[Ability[_]]], List[Principle]] =
    Lens[Map[CharacterComputation.StagingKey, List[Ability[_]]], List[Principle]](
      _.getOrElse(stagingKey, List()).collect { case p: Principle => p }
    )(principles => map => map + (stagingKey -> principles))

  /** Lens to extract chosen abilities from ability choice map */
  def chosenAbilitiesFromChoices(stagingKey: CharacterComputation.StagingKey): Lens[Map[CharacterComputation.StagingKey, Map[AbilityKey, ChosenAbility]], List[ChosenAbility]] =
    Lens[Map[CharacterComputation.StagingKey, Map[AbilityKey, ChosenAbility]], List[ChosenAbility]](
      _.getOrElse(stagingKey, Map()).values.filter(_.descriptionFilledOut).toList
    )(abilities => map => {
      val abilityMap = abilities.map(a => a.key -> a).toMap
      map + (stagingKey -> abilityMap)
    })

  /** Focused lens for background validation data */
  def backgroundValidationData: Lens[CharacterData, (Option[Background], Map[CharacterComputation.StagingKey, List[(Quality, Die)]], Map[CharacterComputation.StagingKey, List[Ability[_]]])] =
    Lens[CharacterData, (Option[Background], Map[CharacterComputation.StagingKey, List[(Quality, Die)]], Map[CharacterComputation.StagingKey, List[Ability[_]]])](
      data => (data.background, data.qualityStaging, data.abilityStaging)
    )(tuple => _ => throw new UnsupportedOperationException("Read-only lens"))

  /** Focused lens for power source validation data */
  def powerSourceValidationData: Lens[CharacterData, (Option[Background], Option[PowerSource], Map[CharacterComputation.StagingKey, List[(Power, Die)]], Map[CharacterComputation.StagingKey, List[(Quality, Die)]], Map[CharacterComputation.StagingKey, List[Ability[_]]], Map[CharacterComputation.StagingKey, Map[AbilityKey, ChosenAbility]])] =
    Lens[CharacterData, (Option[Background], Option[PowerSource], Map[CharacterComputation.StagingKey, List[(Power, Die)]], Map[CharacterComputation.StagingKey, List[(Quality, Die)]], Map[CharacterComputation.StagingKey, List[Ability[_]]], Map[CharacterComputation.StagingKey, Map[AbilityKey, ChosenAbility]])](
      data => (data.background, data.powerSource, data.powerStaging, data.qualityStaging, data.abilityStaging, data.abilityChoice)
    )(tuple => _ => throw new UnsupportedOperationException("Read-only lens"))

  /** Focused lens for health validation data */
  def healthValidationData: Lens[CharacterData, Option[Int]] =
    Lens[CharacterData, Option[Int]](_.health)(_ => identity)

end CharacterData