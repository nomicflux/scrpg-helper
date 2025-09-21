package scrpgHelper.chargen

import com.raquo.laminar.api.L.{*, given}
import scrpgHelper.rolls.Die
import scrpgHelper.status.Status

/** Interface for managing reactive signals and observers for character data.
  *
  * This trait encapsulates all the Laminar reactive programming infrastructure
  * needed for the character model. It provides signals for reactive updates
  * and observers for handling user interactions.
  */
trait SignalManager:

  /** Type alias for staging keys that represent different character aspects. */
  type StagingKey = Background | PowerSource | Archetype | Personality | RedAbility.RedAbilityPhase

  // Core state signals

  /** Signal for the character's background selection.
    *
    * @return Signal that emits the current background selection
    */
  def backgroundSignal: Signal[Option[Background]]

  /** Signal for the character's power source selection.
    *
    * @return Signal that emits the current power source selection
    */
  def powerSourceSignal: Signal[Option[PowerSource]]

  /** Signal for the character's archetype selection.
    *
    * @return Signal that emits the current archetype selection
    */
  def archetypeSignal: Signal[Option[Archetype]]

  /** Signal for the character's personality selection.
    *
    * @return Signal that emits the current personality selection
    */
  def personalitySignal: Signal[Option[Personality]]

  /** Signal for the character's health value.
    *
    * @return Signal that emits the current health value
    */
  def healthSignal: Signal[Option[Int]]

  // Computed signals for current view

  /** Signal for all powers in the current character view.
    *
    * @return Signal that emits the list of current powers with dice
    */
  def currentPowersSignal: Signal[List[(Power, Die)]]

  /** Signal for all qualities in the current character view.
    *
    * @return Signal that emits the list of current qualities with dice
    */
  def currentQualitiesSignal: Signal[List[(Quality, Die)]]

  /** Signal for all abilities in the current character view.
    *
    * @return Signal that emits the list of current chosen abilities
    */
  def currentAbilitiesSignal: Signal[List[ChosenAbility]]

  /** Signal for all principles in the current character view.
    *
    * @return Signal that emits the list of current principles
    */
  def currentPrinciplesSignal: Signal[List[Principle]]

  // Health signals

  /** Signal for the red zone health value.
    *
    * @return Signal that emits the red zone health from personality
    */
  def redZoneHealthSignal: Signal[Option[Int]]

  /** Signal for the calculated health value.
    *
    * @return Signal that emits the computed health value
    */
  def calculatedHealthSignal: Signal[Int]

  // Validation signals

  /** Signal indicating if the background selection is valid.
    *
    * @return Signal that emits true if background is valid
    */
  def backgroundValidSignal: Signal[Boolean]

  /** Signal indicating if the power source selection is valid.
    *
    * @return Signal that emits true if power source is valid
    */
  def powerSourceValidSignal: Signal[Boolean]

  /** Signal indicating if the archetype selection is valid.
    *
    * @return Signal that emits true if archetype is valid
    */
  def archetypeValidSignal: Signal[Boolean]

  /** Signal indicating if the personality selection is valid.
    *
    * @return Signal that emits true if personality is valid
    */
  def personalityValidSignal: Signal[Boolean]

  /** Signal indicating if the red abilities selection is valid.
    *
    * @return Signal that emits true if red abilities are valid
    */
  def redAbilitiesValidSignal: Signal[Boolean]

  /** Signal indicating if the health value is valid.
    *
    * @return Signal that emits true if health is valid
    */
  def healthValidSignal: Signal[Boolean]

  /** Signal indicating if the entire character is complete and valid.
    *
    * @return Signal that emits true if character is complete
    */
  def characterCompleteSignal: Signal[Boolean]

  // Export signal

  /** Signal for character export data.
    *
    * @return Signal that emits the character export data
    */
  def exportSignal: Signal[CharacterModelExport]

  // Observers for state changes

  /** Observer for background selection changes.
    *
    * @return Observer that handles background changes
    */
  def backgroundObserver: Observer[Background]

  /** Observer for power source selection changes.
    *
    * @return Observer that handles power source changes
    */
  def powerSourceObserver: Observer[PowerSource]

  /** Observer for archetype selection changes.
    *
    * @return Observer that handles archetype changes
    */
  def archetypeObserver: Observer[Archetype]

  /** Observer for personality selection changes.
    *
    * @return Observer that handles personality changes
    */
  def personalityObserver: Observer[Personality]

  /** Observer for health value changes.
    *
    * @return Observer that handles health changes
    */
  def healthObserver: Observer[Int]

  // Staging-specific signals

  /** Signal for powers in a specific staging context.
    *
    * @param stagingKey Signal containing the staging key to query
    * @return Signal that emits powers for the specified staging key
    */
  def powersSignal(stagingKey: Signal[Option[StagingKey]]): Signal[List[(Power, Die)]]

  /** Signal for qualities in a specific staging context.
    *
    * @param stagingKey Signal containing the staging key to query
    * @return Signal that emits qualities for the specified staging key
    */
  def qualitiesSignal(stagingKey: Signal[Option[StagingKey]]): Signal[List[(Quality, Die)]]

  /** Signal for abilities in a specific staging context.
    *
    * @param stagingKey Signal containing the staging key to query
    * @return Signal that emits abilities for the specified staging key
    */
  def abilitiesSignal(stagingKey: Signal[Option[StagingKey]]): Signal[List[Ability[_]]]

  /** Signal for ability choices in a specific staging context.
    *
    * @param stagingKey The staging key to query
    * @return Signal that emits ability choices for the specified staging key
    */
  def abilityChoicesSignal(stagingKey: StagingKey): Signal[Map[AbilityKey, ChosenAbility]]

  /** Signal indicating if an ability is selected in a specific staging context.
    *
    * @param stagingKey The staging key to query
    * @param ability Signal containing the ability to check
    * @return Signal that emits true if the ability is selected
    */
  def abilitySelectedSignal(stagingKey: StagingKey, ability: Signal[Option[ChosenAbility]]): Signal[Boolean]

  // Staging observers

  /** Observer for adding powers to a staging area.
    *
    * @param stagingKey The staging key to add powers to
    * @return Observer that handles power additions
    */
  def addPowerObserver(stagingKey: StagingKey): Observer[(Power, Die)]

  /** Observer for removing powers from a staging area.
    *
    * @param stagingKey The staging key to remove powers from
    * @return Observer that handles power removals
    */
  def removePowerObserver(stagingKey: StagingKey): Observer[(Power, Die)]

  /** Observer for adding qualities to a staging area.
    *
    * @param stagingKey The staging key to add qualities to
    * @return Observer that handles quality additions
    */
  def addQualityObserver(stagingKey: StagingKey): Observer[(Quality, Die)]

  /** Observer for removing qualities from a staging area.
    *
    * @param stagingKey The staging key to remove qualities from
    * @return Observer that handles quality removals
    */
  def removeQualityObserver(stagingKey: StagingKey): Observer[(Quality, Die)]

  /** Observer for adding abilities to a staging area.
    *
    * @param stagingKey The staging key to add abilities to
    * @return Observer that handles ability additions
    */
  def addAbilityObserver(stagingKey: StagingKey): Observer[Ability[_]]

  /** Observer for removing abilities from a staging area.
    *
    * @param stagingKey The staging key to remove abilities from
    * @return Observer that handles ability removals
    */
  def removeAbilityObserver(stagingKey: StagingKey): Observer[Ability[_]]

  /** Observer for toggling abilities in a staging area.
    *
    * @param stagingKey The staging key to toggle abilities in
    * @return Observer that handles ability toggles
    */
  def toggleAbilityObserver(stagingKey: StagingKey): Observer[ChosenAbility]

  /** Observer for adding ability choices to a staging area.
    *
    * @param stagingKey The staging key to add choices to
    * @param ability The ability template being customized
    * @return Observer that handles ability choice additions
    */
  def addAbilityChoiceObserver(stagingKey: StagingKey, ability: AbilityTemplate): Observer[AbilityChoice]

  /** Observer for removing ability choices from a staging area.
    *
    * @param stagingKey The staging key to remove choices from
    * @param ability The ability template being customized
    * @return Observer that handles ability choice removals
    */
  def removeAbilityChoiceObserver(stagingKey: StagingKey, ability: AbilityTemplate): Observer[AbilityChoice]

  /** Observer for upgrading dice in a staging area.
    *
    * @param stagingKey The staging key containing the target
    * @return Observer that handles die upgrades
    */
  def upgradeObserver(stagingKey: StagingKey): Observer[Quality | Power]

  /** Observer for downgrading dice in a staging area.
    *
    * @param stagingKey The staging key containing the target
    * @return Observer that handles die downgrades
    */
  def downgradeObserver(stagingKey: StagingKey): Observer[Quality | Power]

end SignalManager