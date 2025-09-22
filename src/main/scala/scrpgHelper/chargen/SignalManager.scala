package scrpgHelper.chargen

import com.raquo.laminar.api.L.{*, given}
import scrpgHelper.rolls.Die
import scrpgHelper.status.Status
import scrpgHelper.chargen.characterModel.*

/** Signal manager interface - reactive signal infrastructure.
  *
  * This trait contains all reactive Vars, Signals, and Observers for character management.
  * It handles the reactive UI layer while delegating business logic to pure computation.
  */
trait SignalManager:

  // Type aliases from computation object
  type StagingKey = CharacterComputation.StagingKey
  type DieChange = CharacterComputation.DieChange

  // Core character selections - reactive vars
  val background: Var[Option[Background]]
  val powerSource: Var[Option[PowerSource]]
  val archetype: Var[Option[Archetype]]
  val personality: Var[Option[Personality]]
  val health: Var[Option[Int]]

  // Change observers for core character selections
  val changeBackground: Observer[Background]
  val changePowerSource: Observer[PowerSource]
  val changeArchetype: Observer[Archetype]
  val changePersonality: Observer[Personality]

  // Quality staging system
  val basePersonalityQualities: Map[StagingKey, List[(Quality, Die)]]
  val qualityStagingVar: Var[Map[StagingKey, List[(Quality, Die)]]]
  def qualitiesSignal(stagingKey: Signal[Option[StagingKey]]): Signal[List[(Quality, Die)]]
  def addQuality(stagingKey: StagingKey): Observer[(Quality, Die)]
  def removeQuality(stagingKey: StagingKey): Observer[(Quality, Die)]

  // Power staging system
  val powerStagingVar: Var[Map[StagingKey, List[(Power, Die)]]]
  def powersSignal(stagingKey: Signal[Option[StagingKey]]): Signal[List[(Power, Die)]]
  def addPower(stagingKey: StagingKey): Observer[(Power, Die)]
  def removePower(stagingKey: StagingKey): Observer[(Power, Die)]

  // Ability staging system
  val abilityStagingVar: Var[Map[StagingKey, List[Ability[_]]]]
  def abilitiesSignal(stagingKey: Signal[Option[StagingKey]]): Signal[List[Ability[_]]]
  def addAbility(stagingKey: StagingKey): Observer[Ability[_]]
  def removeAbility(stagingKey: StagingKey): Observer[Ability[_]]
  def abilitySelected(stagingKey: StagingKey, ability: Signal[Option[ChosenAbility]]): Signal[Boolean]
  def toggleAbility(stagingKey: StagingKey): Observer[ChosenAbility]

  // Ability choice system
  val powerSourceAbilities: List[(PowerSource, Map[AbilityKey, ChosenAbility])]
  val archetypeAbilities: List[(Archetype, Map[AbilityKey, ChosenAbility])]
  val personalityAbilities: List[(Personality, Map[AbilityKey, ChosenAbility])]
  val redAbilities: List[(RedAbility.RedAbilityPhase, Map[AbilityKey, ChosenAbility])]
  val baseAbilitiesVal: Map[StagingKey, Map[AbilityKey, ChosenAbility]]
  val abilityChoiceVar: Var[Map[StagingKey, Map[AbilityKey, ChosenAbility]]]
  def abilityChoicesSignal(stagingKey: StagingKey): Signal[Map[AbilityKey, ChosenAbility]]
  def addAbilityChoice(stagingKey: StagingKey, ability: AbilityTemplate): Observer[AbilityChoice]
  def removeAbilityChoice(stagingKey: StagingKey, ability: AbilityTemplate): Observer[AbilityChoice]

  // Die management system
  val dieChangesVar: Var[Map[StagingKey, Map[Quality | Power, DieChange]]]
  def changeDieChanges(key: StagingKey, direction: DieChange): Observer[Quality | Power]
  def upgrade(key: StagingKey): Observer[Quality | Power]
  def downgrade(key: StagingKey): Observer[Quality | Power]
  def allDieChanges(
      dcs: Map[StagingKey, Map[Quality | Power, DieChange]],
      mps: Option[PowerSource],
      mat: Option[Archetype],
      mpt: Option[Personality]
  ): Map[Quality | Power, DieChange]

  // Health observer
  val calcHealth: Observer[Int]

  // Computed signals for current character data
  val allQualitiesSignal: Signal[List[(Quality, Die)]]
  val allPowersSignal: Signal[List[(Power, Die)]]
  val allStagedAbilitiesSignal: Signal[List[ChosenAbility]]
  val allChosenAbilitiesSignal: Signal[List[ChosenAbility]]
  val allPrinciplesSignal: Signal[List[Principle]]
  val allAbilitiesSignal: Signal[List[Ability[_]]]

  // Health calculations
  val redZoneHealthSignal: Signal[Option[Int]]
  val powerQualityHealthSignal: Signal[Int]

  // Validation signals
  val validBackground: Signal[Boolean]
  val validPowerSource: Signal[Boolean]
  val validArchetype: Signal[Boolean]
  val validPersonality: Signal[Boolean]
  val validRedAbilities: Signal[Boolean]
  val validHealth: Signal[Boolean]

  // Export signal
  val forExport: Signal[CharacterModelExport]

end SignalManager