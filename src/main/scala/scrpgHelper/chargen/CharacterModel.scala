package scrpgHelper.chargen

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die
import scrpgHelper.status.Status
import scrpgHelper.chargen.characterModel.*


final class CharacterModel extends SignalManager:

  // Reactive state variables
  val backgroundVar: Var[Option[Background]] = Var(None)
  val powerSourceVar: Var[Option[PowerSource]] = Var(None)
  val archetypeVar: Var[Option[Archetype]] = Var(None)
  val personalityVar: Var[Option[Personality]] = Var(None)
  val healthVar: Var[Option[Int]] = Var(None)

  // Reactive staging vars - initialized with base values that will be computed by signalManager
  val qualityStagingVar: Var[Map[StagingKey, List[(Quality, Die)]]] = Var(
    Personality.personalities.map(p => p -> List((p.baseQuality, Die.d(8)))).toMap
  )
  val powerStagingVar: Var[Map[StagingKey, List[(Power, Die)]]] = Var(Map())
  val abilityStagingVar: Var[Map[StagingKey, List[Ability[_]]]] = Var(Map())
  val abilityChoiceVar: Var[Map[StagingKey, Map[AbilityKey, ChosenAbility]]] = Var({
    val powerSourceAbilities = PowerSource.powerSources.map(ps =>
      ps -> ps.abilityPools.flatMap(ap => ap.abilities.map(a => a.key -> a.toChosenAbility(ap))).toMap
    )
    val archetypeAbilities = Archetype.archetypes.map(at =>
      at -> at.abilityPools.flatMap(ap => ap.abilities.map(a => a.key -> a.toChosenAbility(ap))).toMap
    )
    val personalityAbilities = Personality.personalities.map(pt =>
      pt -> pt.outAbilityPool.abilities.map(a => a.key -> a.toChosenAbility(pt.outAbilityPool)).toMap
    )
    val redAbilities = List(
      RedAbility.redAbilityPhase -> RedAbility.baseRedAbilityPool.abilities
        .map(a => a.key -> a.toChosenAbility(RedAbility.baseRedAbilityPool)).toMap
    )
    (powerSourceAbilities ++ archetypeAbilities ++ personalityAbilities ++ redAbilities).toMap
  })
  val dieChangesVar: Var[Map[StagingKey, Map[Quality | Power, DieChange]]] = Var(Map())

  // Create signal manager to handle reactive signals with simplified constructor
  private lazy val signalManager = CharacterSignalManager(
    backgroundVar,
    powerSourceVar,
    archetypeVar,
    personalityVar,
    healthVar,
    qualityStagingVar,
    powerStagingVar,
    abilityStagingVar,
    abilityChoiceVar,
    dieChangesVar,
    () => data
  )

  // Change observers for core character selections (passthrough)
  val changeBackground: Observer[Background] = signalManager.changeBackground
  val changePowerSource: Observer[PowerSource] = signalManager.changePowerSource
  val changeArchetype: Observer[Archetype] = signalManager.changeArchetype
  val changePersonality: Observer[Personality] = signalManager.changePersonality

  // SignalManager interface implementation - provide reactive vars as required by interface
  val background: Var[Option[Background]] = backgroundVar
  val powerSource: Var[Option[PowerSource]] = powerSourceVar
  val archetype: Var[Option[Archetype]] = archetypeVar
  val personality: Var[Option[Personality]] = personalityVar
  val health: Var[Option[Int]] = healthVar

  // CharacterData access - create an object that implements CharacterData using current reactive state
  def data: CharacterData = new CharacterData:
    def background: Option[Background] = backgroundVar.now()
    def powerSource: Option[PowerSource] = powerSourceVar.now()
    def archetype: Option[Archetype] = archetypeVar.now()
    def personality: Option[Personality] = personalityVar.now()
    def health: Option[Int] = healthVar.now()

    def allQualities: List[(Quality, Die)] = CharacterComputation.computeAllQualities(
      qualityStaging,
      background,
      powerSource,
      archetype,
      personality,
      dieChanges,
      signalManager.allDieChanges
    )

    def allPowers: List[(Power, Die)] = CharacterComputation.computeAllPowers(
      powerStaging,
      background,
      powerSource,
      archetype,
      personality,
      dieChanges,
      signalManager.allDieChanges
    )

    def allStagedAbilities: List[ChosenAbility] = CharacterComputation.computeAllStagedAbilities(
      abilityStaging,
      background,
      powerSource,
      archetype,
      personality
    )

    def allChosenAbilities: List[ChosenAbility] = CharacterComputation.computeAllChosenAbilities(
      abilityChoice,
      background,
      powerSource,
      archetype,
      personality
    )

    def allPrinciples: List[Principle] = CharacterComputation.computeAllPrinciples(
      abilityStaging,
      background,
      archetype
    )

    def allAbilities: List[Ability[_]] = CharacterComputation.computeAllAbilities(
      allStagedAbilities,
      allChosenAbilities,
      allPrinciples
    )

    def redZoneHealth: Option[Int] = CharacterComputation.computeRedZoneHealth(personality)

    def powerQualityHealth: Int = CharacterComputation.computePowerQualityHealth(
      allPowers,
      allQualities,
      archetype,
      personality
    )

    // Staging data access - needed for validation
    def qualityStaging: Map[StagingKey, List[(Quality, Die)]] = qualityStagingVar.now()
    def powerStaging: Map[StagingKey, List[(Power, Die)]] = powerStagingVar.now()
    def abilityStaging: Map[StagingKey, List[Ability[_]]] = abilityStagingVar.now()
    def abilityChoice: Map[StagingKey, Map[AbilityKey, ChosenAbility]] = abilityChoiceVar.now()
    def dieChanges: Map[StagingKey, Map[Quality | Power, DieChange]] = dieChangesVar.now()
    def baseAbilities: Map[StagingKey, Map[AbilityKey, ChosenAbility]] = signalManager.baseAbilitiesVal


  // SignalManager staging operations implementation (passthrough)
  def qualitiesSignal(
      stagingKey: Signal[Option[StagingKey]]
  ): Signal[List[(Quality, Die)]] = signalManager.qualitiesSignal(stagingKey)

  def addQuality(stagingKey: StagingKey): Observer[(Quality, Die)] = signalManager.addQuality(stagingKey)
  def removeQuality(stagingKey: StagingKey): Observer[(Quality, Die)] = signalManager.removeQuality(stagingKey)

  def powersSignal(
      stagingKey: Signal[Option[StagingKey]]
  ): Signal[List[(Power, Die)]] = signalManager.powersSignal(stagingKey)

  def addPower(stagingKey: StagingKey): Observer[(Power, Die)] = signalManager.addPower(stagingKey)
  def removePower(stagingKey: StagingKey): Observer[(Power, Die)] = signalManager.removePower(stagingKey)

  def abilitiesSignal(
      stagingKey: Signal[Option[StagingKey]]
  ): Signal[List[Ability[_]]] = signalManager.abilitiesSignal(stagingKey)

  def addAbility(stagingKey: StagingKey): Observer[Ability[_]] = signalManager.addAbility(stagingKey)
  def removeAbility(stagingKey: StagingKey): Observer[Ability[_]] = signalManager.removeAbility(stagingKey)

  def abilitySelected(
      stagingKey: StagingKey,
      ability: Signal[Option[ChosenAbility]]
  ): Signal[Boolean] = signalManager.abilitySelected(stagingKey, ability)

  def toggleAbility(stagingKey: StagingKey): Observer[ChosenAbility] = signalManager.toggleAbility(stagingKey)


  def abilityChoicesSignal(
      stagingKey: StagingKey
  ): Signal[Map[AbilityKey, ChosenAbility]] = signalManager.abilityChoicesSignal(stagingKey)

  def addAbilityChoice(
      stagingKey: StagingKey,
      ability: AbilityTemplate
  ): Observer[AbilityChoice] = signalManager.addAbilityChoice(stagingKey, ability)

  def removeAbilityChoice(
      stagingKey: StagingKey,
      ability: AbilityTemplate
  ): Observer[AbilityChoice] = signalManager.removeAbilityChoice(stagingKey, ability)

  def changeDieChanges(
      key: StagingKey,
      direction: DieChange
  ): Observer[Quality | Power] = signalManager.changeDieChanges(key, direction)

  def upgrade(key: StagingKey): Observer[Quality | Power] = signalManager.upgrade(key)

  def downgrade(key: StagingKey): Observer[Quality | Power] = signalManager.downgrade(key)

  // SignalManager implementation - reactive signals (passthrough)
  val allQualitiesSignal: Signal[List[(Quality, Die)]] = signalManager.allQualitiesSignal
  val allPowersSignal: Signal[List[(Power, Die)]] = signalManager.allPowersSignal
  val allStagedAbilitiesSignal: Signal[List[ChosenAbility]] = signalManager.allStagedAbilitiesSignal
  val allChosenAbilitiesSignal: Signal[List[ChosenAbility]] = signalManager.allChosenAbilitiesSignal
  val allPrinciplesSignal: Signal[List[Principle]] = signalManager.allPrinciplesSignal
  val allAbilitiesSignal: Signal[List[Ability[_]]] = signalManager.allAbilitiesSignal
  val redZoneHealthSignal: Signal[Option[Int]] = signalManager.redZoneHealthSignal
  val powerQualityHealthSignal: Signal[Int] = signalManager.powerQualityHealthSignal

  // Backward compatibility - provide signals without "Signal" suffix for existing code (passthrough)
  val allQualities: Signal[List[(Quality, Die)]] = signalManager.allQualities
  val allPowers: Signal[List[(Power, Die)]] = signalManager.allPowers
  val allStagedAbilities: Signal[List[ChosenAbility]] = signalManager.allStagedAbilities
  val allChosenAbilities: Signal[List[ChosenAbility]] = signalManager.allChosenAbilities
  val allPrinciples: Signal[List[Principle]] = signalManager.allPrinciples
  val allAbilities: Signal[List[Ability[_]]] = signalManager.allAbilities
  val redZoneHealth: Signal[Option[Int]] = signalManager.redZoneHealth
  val powerQualityHealth: Signal[Int] = signalManager.powerQualityHealth


  val calcHealth: Observer[Int] = signalManager.calcHealth

  // SignalManager interface implementation (passthrough)
  val basePersonalityQualities: Map[StagingKey, List[(Quality, Die)]] = signalManager.basePersonalityQualities
  val powerSourceAbilities: List[(PowerSource, Map[AbilityKey, ChosenAbility])] = signalManager.powerSourceAbilities
  val archetypeAbilities: List[(Archetype, Map[AbilityKey, ChosenAbility])] = signalManager.archetypeAbilities
  val personalityAbilities: List[(Personality, Map[AbilityKey, ChosenAbility])] = signalManager.personalityAbilities
  val redAbilities: List[(RedAbility.RedAbilityPhase, Map[AbilityKey, ChosenAbility])] = signalManager.redAbilities
  val baseAbilitiesVal: Map[StagingKey, Map[AbilityKey, ChosenAbility]] = signalManager.baseAbilitiesVal

  val validBackground: Signal[Boolean] = signalManager.validBackground
  val validPowerSource: Signal[Boolean] = signalManager.validPowerSource
  val validArchetype: Signal[Boolean] = signalManager.validArchetype
  val validPersonality: Signal[Boolean] = signalManager.validPersonality
  val validRedAbilities: Signal[Boolean] = signalManager.validRedAbilities
  val validHealth: Signal[Boolean] = signalManager.validHealth
  val forExport: Signal[CharacterModelExport] = signalManager.forExport

  // allDieChanges passthrough to signalManager
  def allDieChanges(
      dcs: Map[StagingKey, Map[Quality | Power, DieChange]],
      mps: Option[PowerSource],
      mat: Option[Archetype],
      mpt: Option[Personality]
  ): Map[Quality | Power, DieChange] = signalManager.allDieChanges(dcs, mps, mat, mpt)
end CharacterModel
