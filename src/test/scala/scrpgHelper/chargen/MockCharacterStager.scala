package scrpgHelper.chargen

import com.raquo.laminar.api.L.{*, given}
import scrpgHelper.rolls.Die

/** Mock implementation of CharacterStaging for deterministic testing.
  *
  * Implements CharacterStaging with deterministic data and no-op observers.
  * Returns raw values whenever possible, creates Signals only when required by trait.
  */
class MockCharacterStager(
    qualityStagingValue: Map[CharacterStaging#StagingKey, List[(Quality, Die)]] = Map.empty,
    powerStagingValue: Map[CharacterStaging#StagingKey, List[(Power, Die)]] = Map.empty,
    abilityStagingValue: Map[CharacterStaging#StagingKey, List[Ability[_]]] = Map.empty,
    abilityChoiceValue: Map[CharacterStaging#StagingKey, Map[AbilityKey, ChosenAbility]] = Map.empty
) extends CharacterStaging:

  // No-op observers for character selections
  val changeBackground: Observer[Background] = Observer.empty
  val changePowerSource: Observer[PowerSource] = Observer.empty
  val changeArchetype: Observer[Archetype] = Observer.empty
  val changePersonality: Observer[Personality] = Observer.empty

  // Quality staging - deterministic data
  val basePersonalityQualities: Map[StagingKey, List[(Quality, Die)]] = Map.empty
  val qualityStaging: Var[Map[StagingKey, List[(Quality, Die)]]] = Var(qualityStagingValue)

  def qualitiesSignal(stagingKey: Signal[Option[StagingKey]]): Signal[List[(Quality, Die)]] =
    stagingKey.map(_.flatMap(qualityStagingValue.get).getOrElse(List.empty))

  def addQuality(stagingKey: StagingKey): Observer[(Quality, Die)] = Observer.empty
  def removeQuality(stagingKey: StagingKey): Observer[(Quality, Die)] = Observer.empty

  // Power staging - deterministic data
  val powerStaging: Var[Map[StagingKey, List[(Power, Die)]]] = Var(powerStagingValue)

  def powersSignal(stagingKey: Signal[Option[StagingKey]]): Signal[List[(Power, Die)]] =
    stagingKey.map(_.flatMap(powerStagingValue.get).getOrElse(List.empty))

  def addPower(stagingKey: StagingKey): Observer[(Power, Die)] = Observer.empty
  def removePower(stagingKey: StagingKey): Observer[(Power, Die)] = Observer.empty

  // Ability staging - deterministic data
  val abilityStaging: Var[Map[StagingKey, List[Ability[_]]]] = Var(abilityStagingValue)

  def abilitiesSignal(stagingKey: Signal[Option[StagingKey]]): Signal[List[Ability[_]]] =
    stagingKey.map(_.flatMap(abilityStagingValue.get).getOrElse(List.empty))

  def addAbility(stagingKey: StagingKey): Observer[Ability[_]] = Observer.empty
  def removeAbility(stagingKey: StagingKey): Observer[Ability[_]] = Observer.empty

  def abilitySelected(stagingKey: StagingKey, ability: Signal[Option[ChosenAbility]]): Signal[Boolean] =
    Signal.fromValue(false) // Deterministic: no abilities selected

  def toggleAbility(stagingKey: StagingKey): Observer[ChosenAbility] = Observer.empty

  // Ability choice - deterministic data
  val powerSourceAbilities: List[(PowerSource, Map[AbilityKey, ChosenAbility])] = List.empty
  val archetypeAbilities: List[(Archetype, Map[AbilityKey, ChosenAbility])] = List.empty
  val personalityAbilities: List[(Personality, Map[AbilityKey, ChosenAbility])] = List.empty
  val redAbilities: List[(RedAbility.RedAbilityPhase, Map[AbilityKey, ChosenAbility])] = List.empty
  val baseAbilities: Map[StagingKey, Map[AbilityKey, ChosenAbility]] = Map.empty
  val abilityChoice: Var[Map[StagingKey, Map[AbilityKey, ChosenAbility]]] = Var(abilityChoiceValue)

  def abilityChoicesSignal(stagingKey: StagingKey): Signal[Map[AbilityKey, ChosenAbility]] =
    Signal.fromValue(abilityChoiceValue.getOrElse(stagingKey, Map.empty))

  def addAbilityChoice(stagingKey: StagingKey, ability: AbilityTemplate): Observer[AbilityChoice] = Observer.empty
  def removeAbilityChoice(stagingKey: StagingKey, ability: AbilityTemplate): Observer[AbilityChoice] = Observer.empty

  // Die management - deterministic data
  val dieChanges: Var[Map[StagingKey, Map[Quality | Power, DieChange]]] = Var(Map.empty)

  def changeDieChanges(key: StagingKey, direction: DieChange): Observer[Quality | Power] = Observer.empty
  def upgrade(key: StagingKey): Observer[Quality | Power] = Observer.empty
  def downgrade(key: StagingKey): Observer[Quality | Power] = Observer.empty

  def allDieChanges(
      dcs: Map[StagingKey, Map[Quality | Power, DieChange]],
      mps: Option[PowerSource],
      mat: Option[Archetype],
      mpt: Option[Personality]
  ): Map[Quality | Power, DieChange] =
    // Use deterministic raw data computation instead of reactive patterns
    (mps.fold(List())(ps => dcs.get(ps).fold(List())(dcps => dcps.toList)) ++
      mat.fold(List())(at => dcs.get(at).fold(List())(dcat => dcat.toList)) ++
      mpt.fold(List())(pt => dcs.get(pt).fold(List())(dcpt => dcpt.toList))).toMap

  // Health observer
  val calcHealth: Observer[Int] = Observer.empty

end MockCharacterStager

object MockCharacterStager:

  /** Create a minimal mock character stager with default values. */
  def minimal: MockCharacterStager = new MockCharacterStager()

  /** Create a mock character stager with basic staging data. */
  def withBasicStaging: MockCharacterStager = new MockCharacterStager(
    qualityStagingValue = Map(
      Background.created -> List((Quality.alertness, Die.d(6)))
    ),
    powerStagingValue = Map(
      Background.created -> List((Power.strength, Die.d(8)))
    )
  )

end MockCharacterStager