package scrpgHelper.chargen.characterModel

import scrpgHelper.chargen.*

/** Character validation data interface - pure validation functionality.
  *
  * This trait provides pure functions for validating character data without
  * reactive framework dependencies. All methods are synchronous and return
  * boolean validation results directly.
  */
trait CharacterValidationData:

  /** Validate background selection and related staging data.
    *
    * @param data The character data to validate
    * @return true if background is valid, false otherwise
    */
  def validBackground(data: CharacterData): Boolean

  /** Validate power source selection and related staging data.
    *
    * @param data The character data to validate
    * @return true if power source is valid, false otherwise
    */
  def validPowerSource(data: CharacterData): Boolean

  /** Validate archetype selection and related staging data.
    *
    * @param data The character data to validate
    * @return true if archetype is valid, false otherwise
    */
  def validArchetype(data: CharacterData): Boolean

  /** Validate personality selection and related staging data.
    *
    * @param data The character data to validate
    * @return true if personality is valid, false otherwise
    */
  def validPersonality(data: CharacterData): Boolean

  /** Validate red abilities selection and completion.
    *
    * @param data The character data to validate
    * @return true if red abilities are valid, false otherwise
    */
  def validRedAbilities(data: CharacterData): Boolean

  /** Validate health value is set.
    *
    * @param data The character data to validate
    * @return true if health is valid, false otherwise
    */
  def validHealth(data: CharacterData): Boolean

end CharacterValidationData