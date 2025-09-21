package scrpgHelper.chargen.characterModel

import scrpgHelper.rolls.Die
import scrpgHelper.chargen.*

/** Character data interface - pure data access without reactive framework.
  *
  * This trait provides pure functions for accessing character state and computed values.
  * All methods return raw data without Signals or reactive behavior, making it ideal
  * for testing, computation, and scenarios where synchronous data access is needed.
  */
trait CharacterData:

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

end CharacterData