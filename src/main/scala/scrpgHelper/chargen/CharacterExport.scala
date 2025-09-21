package scrpgHelper.chargen

import scrpgHelper.rolls.Die
import scrpgHelper.status.Status

/** Interface for exporting character data to various formats.
  *
  * This interface provides methods to extract and serialize character information
  * from a CharacterState into structured data or text formats.
  */
trait CharacterExport:

  /** Export the complete character state to a structured data object.
    *
    * @param state The character state to export
    * @return A CharacterModelExport containing all character data
    */
  def exportCharacter(state: CharacterState): CharacterModelExport

  /** Render the character as human-readable text.
    *
    * @param state The character state to render
    * @return A formatted string representation of the character
    */
  def renderCharacter(state: CharacterState): String

  /** Export only the character's powers with their dice.
    *
    * @param state The character state to export from
    * @return List of powers paired with their die values
    */
  def exportPowers(state: CharacterState): List[(Power, Die)]

  /** Export only the character's qualities with their dice.
    *
    * @param state The character state to export from
    * @return List of qualities paired with their die values
    */
  def exportQualities(state: CharacterState): List[(Quality, Die)]

  /** Export only the character's chosen abilities.
    *
    * @param state The character state to export from
    * @return List of chosen abilities with their configurations
    */
  def exportAbilities(state: CharacterState): List[ChosenAbility]

  /** Export only the character's principles.
    *
    * @param state The character state to export from
    * @return List of principles
    */
  def exportPrinciples(state: CharacterState): List[Principle]

  /** Export the character's status dice from their personality.
    *
    * @param state The character state to export from
    * @return Map of status types to their die values
    */
  def exportStatusDice(state: CharacterState): Map[Status, Die]

end CharacterExport