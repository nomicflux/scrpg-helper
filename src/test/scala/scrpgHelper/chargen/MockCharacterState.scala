package scrpgHelper.chargen

import com.raquo.laminar.api.L.{*, given}
import scrpgHelper.rolls.Die
import scrpgHelper.status.Status

/** Mock implementation of CharacterState for deterministic testing.
  *
  * Implements CharacterState in the simplest way possible with deterministic data.
  * Tests should only access this via the CharacterState interface.
  */
class MockCharacterState(
    backgroundValue: Option[Background] = None,
    powerSourceValue: Option[PowerSource] = None,
    archetypeValue: Option[Archetype] = None,
    personalityValue: Option[Personality] = None,
    healthValue: Option[Int] = None,
    allQualitiesValue: List[(Quality, Die)] = List.empty,
    allPowersValue: List[(Power, Die)] = List.empty,
    allStagedAbilitiesValue: List[ChosenAbility] = List.empty,
    allChosenAbilitiesValue: List[ChosenAbility] = List.empty,
    allPrinciplesValue: List[Principle] = List.empty,
    allAbilitiesValue: List[Ability[_]] = List.empty,
    redZoneHealthValue: Option[Int] = None,
    powerQualityHealthValue: Int = 4
) extends CharacterState:

  // Core character selections - only Vars because mandated by trait
  val background: Var[Option[Background]] = Var(backgroundValue)
  val powerSource: Var[Option[PowerSource]] = Var(powerSourceValue)
  val archetype: Var[Option[Archetype]] = Var(archetypeValue)
  val personality: Var[Option[Personality]] = Var(personalityValue)
  val health: Var[Option[Int]] = Var(healthValue)

  // Computed signals - only Signals because mandated by trait, take values from deterministic data
  val allQualities: Signal[List[(Quality, Die)]] = Signal.fromValue(allQualitiesValue)
  val allPowers: Signal[List[(Power, Die)]] = Signal.fromValue(allPowersValue)
  val allStagedAbilities: Signal[List[ChosenAbility]] = Signal.fromValue(allStagedAbilitiesValue)
  val allChosenAbilities: Signal[List[ChosenAbility]] = Signal.fromValue(allChosenAbilitiesValue)
  val allPrinciples: Signal[List[Principle]] = Signal.fromValue(allPrinciplesValue)
  val allAbilities: Signal[List[Ability[_]]] = Signal.fromValue(allAbilitiesValue)

  // Health calculations - only Signals because mandated by trait
  val redZoneHealth: Signal[Option[Int]] = Signal.fromValue(redZoneHealthValue)
  val powerQualityHealth: Signal[Int] = Signal.fromValue(powerQualityHealthValue)

end MockCharacterState

object MockCharacterState:

  /** Create a minimal mock character state with default values. */
  def minimal: MockCharacterState = new MockCharacterState()

  /** Create a mock character state with basic selections. */
  def basicSelections: MockCharacterState = new MockCharacterState(
    backgroundValue = Some(Background.created),
    personalityValue = Some(Personality.loneWolf),
    healthValue = Some(15)
  )

  /** Create a mock character state with powers and qualities. */
  def withPowersAndQualities: MockCharacterState = new MockCharacterState(
    backgroundValue = Some(Background.created),
    personalityValue = Some(Personality.loneWolf),
    healthValue = Some(15),
    allPowersValue = List(
      (Power.strength, Die.d(8)),
      (Power.fire, Die.d(6))
    ),
    allQualitiesValue = List(
      (Quality.alertness, Die.d(6)),
      (Quality.fitness, Die.d(8))
    ),
    redZoneHealthValue = Some(10),
    powerQualityHealthValue = 15
  )

end MockCharacterState