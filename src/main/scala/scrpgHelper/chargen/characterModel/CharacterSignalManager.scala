package scrpgHelper.chargen.characterModel

import com.raquo.laminar.api.L.{*, given}
import scrpgHelper.rolls.Die
import scrpgHelper.chargen.*

/** Pure reactive signal management for character data.
  *
  * Handles all reactive signals and computed values derived from character state.
  * Provides the reactive interface while delegating business logic to pure functions.
  */
class CharacterSignalManager(
    backgroundVar: Var[Option[Background]],
    powerSourceVar: Var[Option[PowerSource]],
    archetypeVar: Var[Option[Archetype]],
    personalityVar: Var[Option[Personality]],
    healthVar: Var[Option[Int]],
    val qualityStagingVar: Var[Map[CharacterComputation.StagingKey, List[(Quality, Die)]]],
    val powerStagingVar: Var[Map[CharacterComputation.StagingKey, List[(Power, Die)]]],
    val abilityStagingVar: Var[Map[CharacterComputation.StagingKey, List[Ability[_]]]],
    val abilityChoiceVar: Var[Map[CharacterComputation.StagingKey, Map[AbilityKey, ChosenAbility]]],
    val dieChangesVar: Var[Map[CharacterComputation.StagingKey, Map[Quality | Power, CharacterComputation.DieChange]]],
    dataProvider: () => CharacterData,
    // Required data for SignalManager
    basePersonalityQualitiesVal: Map[CharacterComputation.StagingKey, List[(Quality, Die)]],
    powerSourceAbilitiesVal: List[(PowerSource, Map[AbilityKey, ChosenAbility])],
    archetypeAbilitiesVal: List[(Archetype, Map[AbilityKey, ChosenAbility])],
    personalityAbilitiesVal: List[(Personality, Map[AbilityKey, ChosenAbility])],
    redAbilitiesVal: List[(RedAbility.RedAbilityPhase, Map[AbilityKey, ChosenAbility])],
    baseAbilitiesParam: Map[CharacterComputation.StagingKey, Map[AbilityKey, ChosenAbility]],
    // Observers from CharacterModel
    changeBackgroundVal: Observer[Background],
    changePowerSourceVal: Observer[PowerSource],
    changeArchetypeVal: Observer[Archetype],
    changePersonalityVal: Observer[Personality],
    calcHealthVal: Observer[Int],
    // Observer generators from CharacterModel
    addQualityFunc: CharacterComputation.StagingKey => Observer[(Quality, Die)],
    removeQualityFunc: CharacterComputation.StagingKey => Observer[(Quality, Die)],
    addPowerFunc: CharacterComputation.StagingKey => Observer[(Power, Die)],
    removePowerFunc: CharacterComputation.StagingKey => Observer[(Power, Die)],
    addAbilityFunc: CharacterComputation.StagingKey => Observer[Ability[_]],
    removeAbilityFunc: CharacterComputation.StagingKey => Observer[Ability[_]],
    toggleAbilityFunc: CharacterComputation.StagingKey => Observer[ChosenAbility],
    addAbilityChoiceFunc: (CharacterComputation.StagingKey, AbilityTemplate) => Observer[AbilityChoice],
    removeAbilityChoiceFunc: (CharacterComputation.StagingKey, AbilityTemplate) => Observer[AbilityChoice],
    changeDieChangesFunc: (CharacterComputation.StagingKey, CharacterComputation.DieChange) => Observer[Quality | Power],
    upgradeFunc: CharacterComputation.StagingKey => Observer[Quality | Power],
    downgradeFunc: CharacterComputation.StagingKey => Observer[Quality | Power],
    allDieChangesFunc: (Map[CharacterComputation.StagingKey, Map[Quality | Power, CharacterComputation.DieChange]], Option[PowerSource], Option[Archetype], Option[Personality]) => Map[Quality | Power, CharacterComputation.DieChange],
    // Validation and export signals
    validBackgroundVal: Signal[Boolean],
    validPowerSourceVal: Signal[Boolean],
    validArchetypeVal: Signal[Boolean],
    validPersonalityVal: Signal[Boolean],
    validRedAbilitiesVal: Signal[Boolean],
    validHealthVal: Signal[Boolean],
    forExportVal: Signal[CharacterModelExport]
) extends SignalManager:

  // SignalManager interface implementation - core character selections
  val background: Var[Option[Background]] = backgroundVar
  val powerSource: Var[Option[PowerSource]] = powerSourceVar
  val archetype: Var[Option[Archetype]] = archetypeVar
  val personality: Var[Option[Personality]] = personalityVar
  val health: Var[Option[Int]] = healthVar

  // Change observers for core character selections
  val changeBackground: Observer[Background] = changeBackgroundVal
  val changePowerSource: Observer[PowerSource] = changePowerSourceVal
  val changeArchetype: Observer[Archetype] = changeArchetypeVal
  val changePersonality: Observer[Personality] = changePersonalityVal

  // Interface implementation via functions - delegate to passed-in functions
  def addQuality(stagingKey: StagingKey): Observer[(Quality, Die)] = addQualityFunc(stagingKey)
  def removeQuality(stagingKey: StagingKey): Observer[(Quality, Die)] = removeQualityFunc(stagingKey)
  def addPower(stagingKey: StagingKey): Observer[(Power, Die)] = addPowerFunc(stagingKey)
  def removePower(stagingKey: StagingKey): Observer[(Power, Die)] = removePowerFunc(stagingKey)
  def addAbility(stagingKey: StagingKey): Observer[Ability[_]] = addAbilityFunc(stagingKey)
  def removeAbility(stagingKey: StagingKey): Observer[Ability[_]] = removeAbilityFunc(stagingKey)
  def toggleAbility(stagingKey: StagingKey): Observer[ChosenAbility] = toggleAbilityFunc(stagingKey)
  def addAbilityChoice(stagingKey: StagingKey, ability: AbilityTemplate): Observer[AbilityChoice] = addAbilityChoiceFunc(stagingKey, ability)
  def removeAbilityChoice(stagingKey: StagingKey, ability: AbilityTemplate): Observer[AbilityChoice] = removeAbilityChoiceFunc(stagingKey, ability)
  def changeDieChanges(key: StagingKey, direction: DieChange): Observer[Quality | Power] = changeDieChangesFunc(key, direction)
  def upgrade(key: StagingKey): Observer[Quality | Power] = upgradeFunc(key)
  def downgrade(key: StagingKey): Observer[Quality | Power] = downgradeFunc(key)
  def allDieChanges(
      dcs: Map[StagingKey, Map[Quality | Power, DieChange]],
      mps: Option[PowerSource],
      mat: Option[Archetype],
      mpt: Option[Personality]
  ): Map[Quality | Power, DieChange] = allDieChangesFunc(dcs, mps, mat, mpt)

  // Interface implementation via constructor parameters - these become the interface properties
  val basePersonalityQualities: Map[StagingKey, List[(Quality, Die)]] = basePersonalityQualitiesVal
  val powerSourceAbilities: List[(PowerSource, Map[AbilityKey, ChosenAbility])] = powerSourceAbilitiesVal
  val archetypeAbilities: List[(Archetype, Map[AbilityKey, ChosenAbility])] = archetypeAbilitiesVal
  val personalityAbilities: List[(Personality, Map[AbilityKey, ChosenAbility])] = personalityAbilitiesVal
  val redAbilities: List[(RedAbility.RedAbilityPhase, Map[AbilityKey, ChosenAbility])] = redAbilitiesVal
  val baseAbilitiesVal: Map[StagingKey, Map[AbilityKey, ChosenAbility]] = baseAbilitiesParam

  val calcHealth: Observer[Int] = calcHealthVal
  val validBackground: Signal[Boolean] = validBackgroundVal
  val validPowerSource: Signal[Boolean] = validPowerSourceVal
  val validArchetype: Signal[Boolean] = validArchetypeVal
  val validPersonality: Signal[Boolean] = validPersonalityVal
  val validRedAbilities: Signal[Boolean] = validRedAbilitiesVal
  val validHealth: Signal[Boolean] = validHealthVal
  val forExport: Signal[CharacterModelExport] = forExportVal

  // Staging signal operations
  def qualitiesSignal(
      stagingKey: Signal[Option[CharacterComputation.StagingKey]]
  ): Signal[List[(Quality, Die)]] =
    qualityStagingVar.signal
      .combineWith(stagingKey)
      .map((m, mb) => mb.flatMap(b => m.get(b)).getOrElse(List()))

  def powersSignal(
      stagingKey: Signal[Option[CharacterComputation.StagingKey]]
  ): Signal[List[(Power, Die)]] =
    powerStagingVar.signal
      .combineWith(stagingKey)
      .map((m, mps) => mps.flatMap(ps => m.get(ps)).getOrElse(List()))

  def abilitiesSignal(
      stagingKey: Signal[Option[CharacterComputation.StagingKey]]
  ): Signal[List[Ability[_]]] =
    abilityStagingVar.signal
      .combineWith(stagingKey)
      .map((m, mb) => mb.flatMap(b => m.get(b)).getOrElse(List()))

  def abilitySelected(
      stagingKey: CharacterComputation.StagingKey,
      ability: Signal[Option[ChosenAbility]]
  ): Signal[Boolean] =
    abilityStagingVar.signal.combineWith(ability).map { (as, ma) =>
      val currListKeys = as.getOrElse(stagingKey, List()).map(_.key).toSet
      ma.fold(false)(a => currListKeys.contains(a.key))
    }

  def abilityChoicesSignal(
      stagingKey: CharacterComputation.StagingKey
  ): Signal[Map[AbilityKey, ChosenAbility]] =
    abilityChoiceVar.signal.map(acs => acs.getOrElse(stagingKey, Map()))

  // SignalManager implementation - reactive signals that call pure CharacterData methods
  val allQualitiesSignal: Signal[List[(Quality, Die)]] = qualityStagingVar.signal
    .combineWith(
      backgroundVar.signal,
      powerSourceVar.signal,
      archetypeVar.signal,
      personalityVar.signal,
      dieChangesVar.signal
    )
    .map((_, _, _, _, _, _) => dataProvider().allQualities)

  val allPowersSignal: Signal[List[(Power, Die)]] = powerStagingVar.signal
    .combineWith(
      backgroundVar.signal,
      powerSourceVar.signal,
      archetypeVar.signal,
      personalityVar.signal,
      dieChangesVar.signal
    )
    .map((_, _, _, _, _, _) => dataProvider().allPowers)

  val allStagedAbilitiesSignal: Signal[List[ChosenAbility]] = abilityStagingVar.signal
    .combineWith(
      backgroundVar.signal,
      powerSourceVar.signal,
      archetypeVar.signal,
      personalityVar.signal
    )
    .map((_, _, _, _, _) => dataProvider().allStagedAbilities)

  val allChosenAbilitiesSignal: Signal[List[ChosenAbility]] = abilityChoiceVar.signal
    .combineWith(
      backgroundVar.signal,
      powerSourceVar.signal,
      archetypeVar.signal,
      personalityVar.signal
    )
    .map((_, _, _, _, _) => dataProvider().allChosenAbilities)

  val allPrinciplesSignal: Signal[List[Principle]] = abilityStagingVar.signal
    .combineWith(
      backgroundVar.signal,
      archetypeVar.signal
    )
    .map((_, _, _) => dataProvider().allPrinciples)

  val allAbilitiesSignal: Signal[List[Ability[_]]] =
    allStagedAbilitiesSignal
      .combineWith(allChosenAbilitiesSignal, allPrinciplesSignal)
      .map((_, _, _) => dataProvider().allAbilities)

  val redZoneHealthSignal: Signal[Option[Int]] =
    personalityVar.signal.map(_ => dataProvider().redZoneHealth)

  val powerQualityHealthSignal: Signal[Int] =
    allPowersSignal
      .combineWith(allQualitiesSignal)
      .combineWith(archetypeVar.signal)
      .combineWith(personalityVar.signal)
      .map((_, _, _, _) => dataProvider().powerQualityHealth)

  // Backward compatibility - provide signals without "Signal" suffix for existing code
  val allQualities: Signal[List[(Quality, Die)]] = allQualitiesSignal
  val allPowers: Signal[List[(Power, Die)]] = allPowersSignal
  val allStagedAbilities: Signal[List[ChosenAbility]] = allStagedAbilitiesSignal
  val allChosenAbilities: Signal[List[ChosenAbility]] = allChosenAbilitiesSignal
  val allPrinciples: Signal[List[Principle]] = allPrinciplesSignal
  val allAbilities: Signal[List[Ability[_]]] = allAbilitiesSignal
  val redZoneHealth: Signal[Option[Int]] = redZoneHealthSignal
  val powerQualityHealth: Signal[Int] = powerQualityHealthSignal

end CharacterSignalManager