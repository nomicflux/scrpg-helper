package scrpgHelper.chargen

import com.raquo.laminar.api.L.{*, given}
import scrpgHelper.rolls.Die
import scrpgHelper.chargen.characterModel.*

/** Character validator implementation - validates character data.
  *
  * Takes CharacterData and implements both CharacterValidationData (pure) and
  * CharacterValidation (reactive) by delegating to pure computation logic.
  */
class CharacterValidator(characterData: CharacterData) extends CharacterValidationData with CharacterValidation:

  // Pure data implementations
  def validBackground(data: CharacterData): Boolean =
    CharacterValidationComputation.validateBackground(data)

  def validPowerSource(data: CharacterData): Boolean =
    CharacterValidationComputation.validatePowerSource(data)

  def validArchetype(data: CharacterData): Boolean =
    CharacterValidationComputation.validateArchetype(data)

  def validPersonality(data: CharacterData): Boolean =
    CharacterValidationComputation.validatePersonality(data)

  def validRedAbilities(data: CharacterData): Boolean =
    CharacterValidationComputation.validateRedAbilities(data)

  def validHealth(data: CharacterData): Boolean =
    CharacterValidationComputation.validateHealth(data)

  // Reactive implementations - create constant signals from current data
  val validBackground: Signal[Boolean] = Signal.fromValue(validBackground(characterData))

  val validPowerSource: Signal[Boolean] = Signal.fromValue(validPowerSource(characterData))
  val validArchetype: Signal[Boolean] = Signal.fromValue(validArchetype(characterData))
  val validPersonality: Signal[Boolean] = Signal.fromValue(validPersonality(characterData))
  val validRedAbilities: Signal[Boolean] = Signal.fromValue(validRedAbilities(characterData))
  val validHealth: Signal[Boolean] = Signal.fromValue(validHealth(characterData))

end CharacterValidator