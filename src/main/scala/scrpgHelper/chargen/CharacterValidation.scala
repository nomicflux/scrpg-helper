package scrpgHelper.chargen

/** Result of a validation operation.
  *
  * @param isValid True if the validation passed, false otherwise
  * @param errors List of error messages describing validation failures
  */
case class ValidationResult(isValid: Boolean, errors: List[String] = List.empty)

object ValidationResult:
  /** Create a successful validation result. */
  def success: ValidationResult = ValidationResult(true)

  /** Create a failed validation result with error messages.
    *
    * @param errors The error messages describing the failures
    * @return A failed validation result
    */
  def failure(errors: String*): ValidationResult = ValidationResult(false, errors.toList)

end ValidationResult

/** Interface for validating character state and staging operations.
  *
  * This trait provides methods to validate different aspects of a character
  * to ensure they meet the game rules and requirements. Validation can be
  * performed at both the boolean level (quick checks) and detailed level
  * (with specific error messages).
  */
trait CharacterValidation:

  /** Check if the character's background selection is valid.
    *
    * @param state The current character state
    * @param staging The character staging operations
    * @return True if the background is valid, false otherwise
    */
  def isBackgroundValid(state: CharacterState, staging: CharacterStaging): Boolean

  /** Check if the character's power source selection is valid.
    *
    * @param state The current character state
    * @param staging The character staging operations
    * @return True if the power source is valid, false otherwise
    */
  def isPowerSourceValid(state: CharacterState, staging: CharacterStaging): Boolean

  /** Check if the character's archetype selection is valid.
    *
    * @param state The current character state
    * @param staging The character staging operations
    * @return True if the archetype is valid, false otherwise
    */
  def isArchetypeValid(state: CharacterState, staging: CharacterStaging): Boolean

  /** Check if the character's personality selection is valid.
    *
    * @param state The current character state
    * @param staging The character staging operations
    * @return True if the personality is valid, false otherwise
    */
  def isPersonalityValid(state: CharacterState, staging: CharacterStaging): Boolean

  /** Check if the character's red abilities selection is valid.
    *
    * @param state The current character state
    * @param staging The character staging operations
    * @return True if the red abilities are valid, false otherwise
    */
  def areRedAbilitiesValid(state: CharacterState, staging: CharacterStaging): Boolean

  /** Check if the character's health value is valid.
    *
    * @param state The current character state
    * @return True if the health is valid, false otherwise
    */
  def isHealthValid(state: CharacterState): Boolean

  /** Check if the entire character is complete and valid.
    *
    * @param state The current character state
    * @param staging The character staging operations
    * @return True if the character is complete and valid, false otherwise
    */
  def isCharacterComplete(state: CharacterState, staging: CharacterStaging): Boolean

  /** Validate the character's background selection with detailed errors.
    *
    * @param state The current character state
    * @param staging The character staging operations
    * @return Validation result with specific error messages if invalid
    */
  def validateBackground(state: CharacterState, staging: CharacterStaging): ValidationResult

  /** Validate the character's power source selection with detailed errors.
    *
    * @param state The current character state
    * @param staging The character staging operations
    * @return Validation result with specific error messages if invalid
    */
  def validatePowerSource(state: CharacterState, staging: CharacterStaging): ValidationResult

  /** Validate the character's archetype selection with detailed errors.
    *
    * @param state The current character state
    * @param staging The character staging operations
    * @return Validation result with specific error messages if invalid
    */
  def validateArchetype(state: CharacterState, staging: CharacterStaging): ValidationResult

  /** Validate the character's personality selection with detailed errors.
    *
    * @param state The current character state
    * @param staging The character staging operations
    * @return Validation result with specific error messages if invalid
    */
  def validatePersonality(state: CharacterState, staging: CharacterStaging): ValidationResult

  /** Validate the character's red abilities selection with detailed errors.
    *
    * @param state The current character state
    * @param staging The character staging operations
    * @return Validation result with specific error messages if invalid
    */
  def validateRedAbilities(state: CharacterState, staging: CharacterStaging): ValidationResult

  /** Validate the character's health value with detailed errors.
    *
    * @param state The current character state
    * @return Validation result with specific error messages if invalid
    */
  def validateHealth(state: CharacterState): ValidationResult

  /** Validate the entire character with detailed errors for all aspects.
    *
    * @param state The current character state
    * @param staging The character staging operations
    * @return List of validation results for each aspect of the character
    */
  def validateCharacter(state: CharacterState, staging: CharacterStaging): List[ValidationResult]

end CharacterValidation