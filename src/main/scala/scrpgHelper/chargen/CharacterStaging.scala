package scrpgHelper.chargen

import com.raquo.laminar.api.L.{*, given}
import scrpgHelper.rolls.Die

/** Character staging interface - staging operations and state changes.
  *
  * This trait captures all staging operations, change observers, die management,
  * and ability choice management with view preservation.
  */
trait CharacterStaging:

  // Type definitions
  type StagingKey = Background | PowerSource | Archetype | Personality | RedAbility.RedAbilityPhase

  // Die change enum
  enum DieChange:
    case Upgrade, Downgrade

    def onDie(d: Die): Die = this match
      case Upgrade   => d.upgrade
      case Downgrade => d.downgrade
  end DieChange

  object DieChange:
    def combine(
        ths: Option[DieChange],
        that: Option[DieChange]
    ): Option[DieChange] =
      (ths, that) match
        case (None, None)       => None
        case (Some(x), None)    => Some(x)
        case (None, Some(y))    => Some(y)
        case (Some(x), Some(y)) => if x == y then Some(x) else None
  end DieChange

  // Change observers for core character selections
  val changeBackground: Observer[Background]
  val changePowerSource: Observer[PowerSource]
  val changeArchetype: Observer[Archetype]
  val changePersonality: Observer[Personality]

  // Quality staging system
  val basePersonalityQualities: Map[StagingKey, List[(Quality, Die)]]
  val qualityStaging: Var[Map[StagingKey, List[(Quality, Die)]]]
  def qualitiesSignal(stagingKey: Signal[Option[StagingKey]]): Signal[List[(Quality, Die)]]
  def addQuality(stagingKey: StagingKey): Observer[(Quality, Die)]
  def removeQuality(stagingKey: StagingKey): Observer[(Quality, Die)]

  // Power staging system
  val powerStaging: Var[Map[StagingKey, List[(Power, Die)]]]
  def powersSignal(stagingKey: Signal[Option[StagingKey]]): Signal[List[(Power, Die)]]
  def addPower(stagingKey: StagingKey): Observer[(Power, Die)]
  def removePower(stagingKey: StagingKey): Observer[(Power, Die)]

  // Ability staging system
  val abilityStaging: Var[Map[StagingKey, List[Ability[_]]]]
  def abilitiesSignal(stagingKey: Signal[Option[StagingKey]]): Signal[List[Ability[_]]]
  def addAbility(stagingKey: StagingKey): Observer[Ability[_]]
  def removeAbility(stagingKey: StagingKey): Observer[Ability[_]]
  def abilitySelected(stagingKey: StagingKey, ability: Signal[Option[ChosenAbility]]): Signal[Boolean]
  def toggleAbility(stagingKey: StagingKey): Observer[ChosenAbility]

  // Ability choice system
  val powerSourceAbilities: List[(PowerSource, Map[AbilityKey, ChosenAbility])]
  val archetypeAbilities: List[(Archetype, Map[AbilityKey, ChosenAbility])]
  val personalityAbilities: List[(Personality, Map[AbilityKey, ChosenAbility])]
  val redAbilities: List[(RedAbility.RedAbilityPhase, Map[AbilityKey, ChosenAbility])]
  val baseAbilities: Map[StagingKey, Map[AbilityKey, ChosenAbility]]
  val abilityChoice: Var[Map[StagingKey, Map[AbilityKey, ChosenAbility]]]
  def abilityChoicesSignal(stagingKey: StagingKey): Signal[Map[AbilityKey, ChosenAbility]]
  def addAbilityChoice(stagingKey: StagingKey, ability: AbilityTemplate): Observer[AbilityChoice]
  def removeAbilityChoice(stagingKey: StagingKey, ability: AbilityTemplate): Observer[AbilityChoice]

  // Die management system
  val dieChanges: Var[Map[StagingKey, Map[Quality | Power, DieChange]]]
  def changeDieChanges(key: StagingKey, direction: DieChange): Observer[Quality | Power]
  def upgrade(key: StagingKey): Observer[Quality | Power]
  def downgrade(key: StagingKey): Observer[Quality | Power]
  def allDieChanges(
      dcs: Map[StagingKey, Map[Quality | Power, DieChange]],
      mps: Option[PowerSource],
      mat: Option[Archetype],
      mpt: Option[Personality]
  ): Map[Quality | Power, DieChange]

  // Health observer
  val calcHealth: Observer[Int]

end CharacterStaging