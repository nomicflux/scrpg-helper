package scrpgHelper.chargen

import scrpgHelper.rolls.Die
import scrpgHelper.status.Status

/** Mock implementation of CharacterState for deterministic testing.
  *
  * This implementation provides a purely deterministic character state
  * without any reactive signals or dependencies on external frameworks.
  * All data can be set directly for testing purposes.
  *
  * @param _background The character's background
  * @param _powerSource The character's power source
  * @param _archetype The character's archetype
  * @param _personality The character's personality
  * @param _health The character's health value
  * @param _currentPowers List of powers with dice
  * @param _currentQualities List of qualities with dice
  * @param _currentAbilities List of chosen abilities
  * @param _currentPrinciples List of principles
  * @param _redZoneHealth Red zone health value
  * @param _calculatedHealth Calculated health value
  * @param _statusDice Map of status dice
  */
class MockCharacterState(
    private val _background: Option[Background] = None,
    private val _powerSource: Option[PowerSource] = None,
    private val _archetype: Option[Archetype] = None,
    private val _personality: Option[Personality] = None,
    private val _health: Option[Int] = None,
    private val _currentPowers: List[(Power, Die)] = List.empty,
    private val _currentQualities: List[(Quality, Die)] = List.empty,
    private val _currentAbilities: List[ChosenAbility] = List.empty,
    private val _currentPrinciples: List[Principle] = List.empty,
    private val _redZoneHealth: Option[Int] = None,
    private val _calculatedHealth: Int = 4,
    private val _statusDice: Map[Status, Die] = Map.empty
) extends CharacterState:

  /** The character's selected background.
    *
    * @return Some(background) if selected, None otherwise
    */
  def background: Option[Background] = _background

  /** The character's selected power source.
    *
    * @return Some(powerSource) if selected, None otherwise
    */
  def powerSource: Option[PowerSource] = _powerSource

  /** The character's selected archetype.
    *
    * @return Some(archetype) if selected, None otherwise
    */
  def archetype: Option[Archetype] = _archetype

  /** The character's selected personality.
    *
    * @return Some(personality) if selected, None otherwise
    */
  def personality: Option[Personality] = _personality

  /** The character's health value.
    *
    * @return Some(health) if set, None otherwise
    */
  def health: Option[Int] = _health

  /** All powers currently active for this character based on selections.
    *
    * @return List of powers paired with their current die values
    */
  def currentPowers: List[(Power, Die)] = _currentPowers

  /** All qualities currently active for this character based on selections.
    *
    * @return List of qualities paired with their current die values
    */
  def currentQualities: List[(Quality, Die)] = _currentQualities

  /** All chosen abilities currently active for this character.
    *
    * @return List of chosen abilities with their configurations
    */
  def currentAbilities: List[ChosenAbility] = _currentAbilities

  /** All principles currently active for this character.
    *
    * @return List of principles
    */
  def currentPrinciples: List[Principle] = _currentPrinciples

  /** The red zone health value from the character's personality.
    *
    * @return Some(redZoneHealth) if personality is selected and has red status die, None otherwise
    */
  def redZoneHealth: Option[Int] = _redZoneHealth

  /** The calculated health value based on powers and qualities.
    *
    * @return The calculated health value
    */
  def calculatedHealth: Int = _calculatedHealth

  /** The status dice from the character's personality.
    *
    * @return Map of status types to their die values, empty if no personality selected
    */
  def statusDice: Map[Status, Die] = _statusDice

end MockCharacterState

object MockCharacterState:
  /** Create a minimal mock character state with default values. */
  def minimal: MockCharacterState = new MockCharacterState()

  /** Create a complete mock character state with all fields populated. */
  def complete: MockCharacterState = new MockCharacterState(
    _background = Some(Background.created),
    _powerSource = None, // Will need actual PowerSource instance
    _archetype = None,   // Will need actual Archetype instance
    _personality = None, // Will need actual Personality instance
    _health = Some(15),
    _currentPowers = List((Power.flight, Die.d(8))),
    _currentQualities = List((Quality.alertness, Die.d(6))),
    _currentAbilities = List.empty, // Will need actual ChosenAbility instances
    _currentPrinciples = List.empty, // Will need actual Principle instances
    _redZoneHealth = Some(10),
    _calculatedHealth = 15,
    _statusDice = Map(
      Status.Green -> Die.d(6),
      Status.Yellow -> Die.d(8),
      Status.Red -> Die.d(10)
    )
  )

end MockCharacterState