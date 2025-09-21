package scrpgHelper.chargen

import com.raquo.laminar.api.L.{*, given}
import scrpgHelper.rolls.Die
import scrpgHelper.status.Status

/** Character state interface - core character selections and computed values.
  *
  * This trait captures the current character state including selections
  * and computed signals for powers, qualities, abilities, and health.
  */
trait CharacterState:

  // Core character selections
  val background: Var[Option[Background]]
  val powerSource: Var[Option[PowerSource]]
  val archetype: Var[Option[Archetype]]
  val personality: Var[Option[Personality]]
  val health: Var[Option[Int]]

  // Computed signals for current character data
  val allQualities: Signal[List[(Quality, Die)]]
  val allPowers: Signal[List[(Power, Die)]]
  val allStagedAbilities: Signal[List[ChosenAbility]]
  val allChosenAbilities: Signal[List[ChosenAbility]]
  val allPrinciples: Signal[List[Principle]]
  val allAbilities: Signal[List[Ability[_]]]

  // Health calculations
  val redZoneHealth: Signal[Option[Int]]
  val powerQualityHealth: Signal[Int]

end CharacterState