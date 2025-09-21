package scrpgHelper.chargen

import com.raquo.laminar.api.L.{*, given}

/** Character validation interface - validation signals.
  *
  * This trait captures all validation signals from CharacterModel.
  */
trait CharacterValidation:

  // Validation signals
  val validBackground: Signal[Boolean]
  val validPowerSource: Signal[Boolean]
  val validArchetype: Signal[Boolean]
  val validPersonality: Signal[Boolean]
  val validRedAbilities: Signal[Boolean]
  val validHealth: Signal[Boolean]

end CharacterValidation