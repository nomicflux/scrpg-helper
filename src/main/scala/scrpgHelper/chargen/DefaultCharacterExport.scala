package scrpgHelper.chargen

import scrpgHelper.rolls.Die
import scrpgHelper.status.Status

/** Default implementation of CharacterExport.
  *
  * This implementation extracts character data from a CharacterState
  * and provides various export formats.
  */
class DefaultCharacterExport extends CharacterExport:

  /** Export the complete character state to a structured data object.
    *
    * @param state The character state to export
    * @return A CharacterModelExport containing all character data
    */
  def exportCharacter(state: CharacterState): CharacterModelExport =
    CharacterModelExport(
      background = state.background.map(_.name),
      powerSource = state.powerSource.map(_.name),
      archetype = state.archetype.map(_.name),
      personality = state.personality.map(_.name),
      statuses = state.statusDice,
      health = state.health,
      powers = state.currentPowers,
      qualities = state.currentQualities,
      abilities = state.currentAbilities,
      principles = state.currentPrinciples
    )

  /** Render the character as human-readable text.
    *
    * @param state The character state to render
    * @return A formatted string representation of the character
    */
  def renderCharacter(state: CharacterState): String =
    exportCharacter(state).render

  /** Export only the character's powers with their dice.
    *
    * @param state The character state to export from
    * @return List of powers paired with their die values
    */
  def exportPowers(state: CharacterState): List[(Power, Die)] =
    state.currentPowers

  /** Export only the character's qualities with their dice.
    *
    * @param state The character state to export from
    * @return List of qualities paired with their die values
    */
  def exportQualities(state: CharacterState): List[(Quality, Die)] =
    state.currentQualities

  /** Export only the character's chosen abilities.
    *
    * @param state The character state to export from
    * @return List of chosen abilities with their configurations
    */
  def exportAbilities(state: CharacterState): List[ChosenAbility] =
    state.currentAbilities

  /** Export only the character's principles.
    *
    * @param state The character state to export from
    * @return List of principles
    */
  def exportPrinciples(state: CharacterState): List[Principle] =
    state.currentPrinciples

  /** Export the character's status dice from their personality.
    *
    * @param state The character state to export from
    * @return Map of status types to their die values
    */
  def exportStatusDice(state: CharacterState): Map[Status, Die] =
    state.statusDice

end DefaultCharacterExport