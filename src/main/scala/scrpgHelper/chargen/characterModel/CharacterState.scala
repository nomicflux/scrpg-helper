package scrpgHelper.chargen.characterModel

import com.raquo.laminar.api.L.{*, given}
import scrpgHelper.rolls.Die
import scrpgHelper.status.Status
import scrpgHelper.chargen.*

/** Character state interface - reactive wrapper around character data.
  *
  * This trait provides both pure data access via the data property and reactive
  * signals for UI components. The reactive signals are derived from the pure data.
  */
trait CharacterState:

  // Access to pure character data
  val data: CharacterData

  // Core character selections - reactive vars
  val background: Var[Option[Background]]
  val powerSource: Var[Option[PowerSource]]
  val archetype: Var[Option[Archetype]]
  val personality: Var[Option[Personality]]
  val health: Var[Option[Int]]

  // Computed signals for current character data - reactive wrappers
  val allQualities: Signal[List[(Quality, Die)]]
  val allPowers: Signal[List[(Power, Die)]]
  val allStagedAbilities: Signal[List[ChosenAbility]]
  val allChosenAbilities: Signal[List[ChosenAbility]]
  val allPrinciples: Signal[List[Principle]]
  val allAbilities: Signal[List[Ability[_]]]

  // Health calculations - reactive wrappers
  val redZoneHealth: Signal[Option[Int]]
  val powerQualityHealth: Signal[Int]

end CharacterState