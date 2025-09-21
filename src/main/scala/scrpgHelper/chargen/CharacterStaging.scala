package scrpgHelper.chargen

import scrpgHelper.rolls.Die

/** Interface for managing character staging operations with view preservation.
  *
  * This trait provides methods to stage character choices across different
  * configurations while preserving selections when switching between views.
  * For example, if abilities are chosen for Archetype A, those choices are
  * preserved even when the current view switches to Archetype B.
  */
trait CharacterStaging:

  /** Type alias for staging keys that represent different character aspects. */
  type StagingKey = Background | PowerSource | Archetype | Personality | RedAbility.RedAbilityPhase

  /** Change the character's background selection.
    *
    * @param background The new background to select
    */
  def changeBackground(background: Background): Unit

  /** Change the character's power source selection.
    *
    * @param powerSource The new power source to select
    */
  def changePowerSource(powerSource: PowerSource): Unit

  /** Change the character's archetype selection.
    *
    * @param archetype The new archetype to select
    */
  def changeArchetype(archetype: Archetype): Unit

  /** Change the character's personality selection.
    *
    * @param personality The new personality to select
    */
  def changePersonality(personality: Personality): Unit

  /** Set the character's health value.
    *
    * @param health The health value to set
    */
  def setHealth(health: Int): Unit

  /** Add a power to the staging area for a specific character aspect.
    *
    * Powers are preserved per staging key, allowing different power
    * selections for different character configurations.
    *
    * @param stagingKey The character aspect this power belongs to
    * @param power The power to add
    * @param die The die value for this power
    */
  def addPower(stagingKey: StagingKey, power: Power, die: Die): Unit

  /** Remove a power from the staging area for a specific character aspect.
    *
    * @param stagingKey The character aspect to remove the power from
    * @param power The power to remove
    */
  def removePower(stagingKey: StagingKey, power: Power): Unit

  /** Add a quality to the staging area for a specific character aspect.
    *
    * Qualities are preserved per staging key, allowing different quality
    * selections for different character configurations.
    *
    * @param stagingKey The character aspect this quality belongs to
    * @param quality The quality to add
    * @param die The die value for this quality
    */
  def addQuality(stagingKey: StagingKey, quality: Quality, die: Die): Unit

  /** Remove a quality from the staging area for a specific character aspect.
    *
    * @param stagingKey The character aspect to remove the quality from
    * @param quality The quality to remove
    */
  def removeQuality(stagingKey: StagingKey, quality: Quality): Unit

  /** Add an ability to the staging area for a specific character aspect.
    *
    * Abilities are preserved per staging key, allowing different ability
    * selections for different character configurations.
    *
    * @param stagingKey The character aspect this ability belongs to
    * @param ability The ability to add
    */
  def addAbility(stagingKey: StagingKey, ability: Ability[_]): Unit

  /** Remove an ability from the staging area for a specific character aspect.
    *
    * @param stagingKey The character aspect to remove the ability from
    * @param ability The ability to remove
    */
  def removeAbility(stagingKey: StagingKey, ability: Ability[_]): Unit

  /** Toggle an ability's selection state for a specific character aspect.
    *
    * If the ability is currently selected, it will be deselected.
    * If the ability is not selected, it will be selected.
    *
    * @param stagingKey The character aspect this ability belongs to
    * @param ability The chosen ability to toggle
    */
  def toggleAbility(stagingKey: StagingKey, ability: ChosenAbility): Unit

  /** Add an ability choice/customization for a specific character aspect.
    *
    * This allows customizing how abilities work while preserving those
    * customizations per staging key.
    *
    * @param stagingKey The character aspect this choice belongs to
    * @param ability The ability template being customized
    * @param choice The choice/customization to apply
    */
  def addAbilityChoice(stagingKey: StagingKey, ability: AbilityTemplate, choice: AbilityChoice): Unit

  /** Remove an ability choice/customization for a specific character aspect.
    *
    * @param stagingKey The character aspect to remove the choice from
    * @param ability The ability template being customized
    * @param choice The choice/customization to remove
    */
  def removeAbilityChoice(stagingKey: StagingKey, ability: AbilityTemplate, choice: AbilityChoice): Unit

  /** Upgrade a die for a power or quality in a specific character aspect.
    *
    * @param stagingKey The character aspect containing the target
    * @param target The power or quality whose die should be upgraded
    */
  def upgradeDie(stagingKey: StagingKey, target: Quality | Power): Unit

  /** Downgrade a die for a power or quality in a specific character aspect.
    *
    * @param stagingKey The character aspect containing the target
    * @param target The power or quality whose die should be downgraded
    */
  def downgradeDie(stagingKey: StagingKey, target: Quality | Power): Unit

  /** Get all staged powers for a specific character aspect.
    *
    * @param stagingKey The character aspect to query
    * @return List of powers with their dice for this staging key
    */
  def getStagedPowers(stagingKey: StagingKey): List[(Power, Die)]

  /** Get all staged qualities for a specific character aspect.
    *
    * @param stagingKey The character aspect to query
    * @return List of qualities with their dice for this staging key
    */
  def getStagedQualities(stagingKey: StagingKey): List[(Quality, Die)]

  /** Get all staged abilities for a specific character aspect.
    *
    * @param stagingKey The character aspect to query
    * @return List of abilities for this staging key
    */
  def getStagedAbilities(stagingKey: StagingKey): List[Ability[_]]

  /** Get all ability choices/customizations for a specific character aspect.
    *
    * @param stagingKey The character aspect to query
    * @return Map of ability keys to their chosen configurations
    */
  def getAbilityChoices(stagingKey: StagingKey): Map[AbilityKey, ChosenAbility]

end CharacterStaging