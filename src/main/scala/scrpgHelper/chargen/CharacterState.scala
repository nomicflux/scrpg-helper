package scrpgHelper.chargen

import scrpgHelper.rolls.Die
import scrpgHelper.status.Status

/** Interface for accessing the current state of a character.
  *
  * This trait provides read-only access to all character information
  * based on the current selections and computed values. It represents
  * the "getter" interface for character data.
  */
trait CharacterState:

  /** The character's selected background.
    *
    * @return Some(background) if selected, None otherwise
    */
  def background: Option[Background]

  /** The character's selected power source.
    *
    * @return Some(powerSource) if selected, None otherwise
    */
  def powerSource: Option[PowerSource]

  /** The character's selected archetype.
    *
    * @return Some(archetype) if selected, None otherwise
    */
  def archetype: Option[Archetype]

  /** The character's selected personality.
    *
    * @return Some(personality) if selected, None otherwise
    */
  def personality: Option[Personality]

  /** The character's health value.
    *
    * @return Some(health) if set, None otherwise
    */
  def health: Option[Int]

  /** All powers currently active for this character based on selections.
    *
    * This includes powers from background, power source, archetype, and
    * personality, with any die modifications applied.
    *
    * @return List of powers paired with their current die values
    */
  def currentPowers: List[(Power, Die)]

  /** All qualities currently active for this character based on selections.
    *
    * This includes qualities from background, power source, archetype, and
    * personality, with any die modifications applied.
    *
    * @return List of qualities paired with their current die values
    */
  def currentQualities: List[(Quality, Die)]

  /** All chosen abilities currently active for this character.
    *
    * This includes abilities from power source, archetype, personality,
    * and red abilities that have been selected and configured.
    *
    * @return List of chosen abilities with their configurations
    */
  def currentAbilities: List[ChosenAbility]

  /** All principles currently active for this character.
    *
    * This includes principles from background and archetype selections.
    *
    * @return List of principles
    */
  def currentPrinciples: List[Principle]

  /** The red zone health value from the character's personality.
    *
    * @return Some(redZoneHealth) if personality is selected and has red status die, None otherwise
    */
  def redZoneHealth: Option[Int]

  /** The calculated health value based on powers and qualities.
    *
    * This is computed from the highest Athletic power, Mental quality,
    * and any additional health categories from archetype/personality.
    *
    * @return The calculated health value
    */
  def calculatedHealth: Int

  /** The status dice from the character's personality.
    *
    * @return Map of status types to their die values, empty if no personality selected
    */
  def statusDice: Map[Status, Die]

end CharacterState