package scrpgHelper.chargen

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die
import scrpgHelper.status.Status
import scrpgHelper.chargen.characterModel.*


final class CharacterModel extends SignalManager:

  // Base data for initialization - computed values for signalManager
  private val basePersonalityQualitiesVal: Map[StagingKey, List[(Quality, Die)]] =
    Personality.personalities
      .map(p => p -> List((p.baseQuality, Die.d(8))))
      .toMap

  private val powerSourceAbilitiesVal: List[(PowerSource, Map[AbilityKey, ChosenAbility])] =
    PowerSource.powerSources
      .map(ps =>
        ps ->
          (ps.abilityPools
            .flatMap(ap =>
              ap.abilities.map(a => a.key -> a.toChosenAbility(ap))
            )
            .toMap)
      )

  private val archetypeAbilitiesVal: List[(Archetype, Map[AbilityKey, ChosenAbility])] =
    Archetype.archetypes
      .map(at =>
        at ->
          (at.abilityPools
            .flatMap(ap =>
              ap.abilities.map(a => a.key -> a.toChosenAbility(ap))
            )
            .toMap)
      )

  private val personalityAbilitiesVal: List[(Personality, Map[AbilityKey, ChosenAbility])] =
    Personality.personalities
      .map(pt =>
        pt -> (pt.outAbilityPool.abilities
          .map(a => a.key -> a.toChosenAbility(pt.outAbilityPool))
          .toMap)
      )

  private val redAbilitiesVal: List[(RedAbility.RedAbilityPhase, Map[AbilityKey, ChosenAbility])] =
    List(
      RedAbility.redAbilityPhase -> RedAbility.baseRedAbilityPool.abilities
        .map(a => a.key -> a.toChosenAbility(RedAbility.baseRedAbilityPool))
        .toMap
    )

  private val baseAbilitiesParam: Map[StagingKey, Map[AbilityKey, ChosenAbility]] =
    (powerSourceAbilitiesVal ++ archetypeAbilitiesVal ++ personalityAbilitiesVal ++ redAbilitiesVal).toMap

  // Reactive state variables
  val backgroundVar: Var[Option[Background]] = Var(None)
  val powerSourceVar: Var[Option[PowerSource]] = Var(None)
  val archetypeVar: Var[Option[Archetype]] = Var(None)
  val personalityVar: Var[Option[Personality]] = Var(None)
  val healthVar: Var[Option[Int]] = Var(None)

  // Reactive staging vars
  val qualityStagingVar: Var[Map[StagingKey, List[(Quality, Die)]]] = Var(basePersonalityQualitiesVal)
  val powerStagingVar: Var[Map[StagingKey, List[(Power, Die)]]] = Var(Map())
  val abilityStagingVar: Var[Map[StagingKey, List[Ability[_]]]] = Var(Map())
  val abilityChoiceVar: Var[Map[StagingKey, Map[AbilityKey, ChosenAbility]]] = Var(baseAbilitiesParam)
  val dieChangesVar: Var[Map[StagingKey, Map[Quality | Power, DieChange]]] = Var(Map())

  // Create observer functions that don't depend on signalManager
  private val backgroundObserver = backgroundVar.updater { (_, b: Background) => Some(b) }
  private val powerSourceObserver = powerSourceVar.updater { (_, ps: PowerSource) => Some(ps) }
  private val archetypeObserver = archetypeVar.updater { (_, at: Archetype) => Some(at) }
  private val healthObserver = healthVar.updater { (m, n: Int) => if (m.isDefined) then m else Some(n) }

  // Create personality observer that will be defined after signalManager
  private def personalityObserver = personalityVar.updater { (_, p: Personality) =>
    // Toggle ability will be handled by the signalManager after it's created
    Some(p)
  }

  // Create a combined signal that triggers when any relevant data changes
  private val validationTrigger: Signal[Unit] = backgroundVar.signal
    .combineWith(
      powerSourceVar.signal,
      archetypeVar.signal,
      personalityVar.signal,
      healthVar.signal
    )
    .combineWith(
      qualityStagingVar.signal,
      powerStagingVar.signal,
      abilityStagingVar.signal
    )
    .combineWith(
      abilityChoiceVar.signal,
      dieChangesVar.signal
    )
    .map { _ => () }

  // Create signal manager to handle reactive signals
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
    () => data,
    basePersonalityQualitiesVal,
    powerSourceAbilitiesVal,
    archetypeAbilitiesVal,
    personalityAbilitiesVal,
    redAbilitiesVal,
    baseAbilitiesParam,
    backgroundObserver,
    powerSourceObserver,
    archetypeObserver,
    personalityObserver,
    healthObserver,
    (stagingKey: StagingKey) => qualityStagingVar.updater { case (m, (q, d)) =>
      val newList = m.getOrElse(stagingKey, List()) :+ (q, d)
      if (newList == newList.distinct) {
        m + (stagingKey -> newList)
      } else {
        m
      }
    },
    (stagingKey: StagingKey) => qualityStagingVar.updater { case (m, (q, d)) =>
      val newList = m.getOrElse(stagingKey, List()).filter(_._1 != q)
      m + (stagingKey -> newList)
    },
    (stagingKey: StagingKey) => powerStagingVar.updater { case (m, (p, d)) =>
      val newList = m.getOrElse(stagingKey, List()) :+ (p, d)
      m + (stagingKey -> newList)
    },
    (stagingKey: StagingKey) => powerStagingVar.updater { case (m, (p, d)) =>
      val newList = m.getOrElse(stagingKey, List()).filter(_._1 != p)
      m + (stagingKey -> newList)
    },
    (stagingKey: StagingKey) => abilityStagingVar.updater { (m, a) =>
      val newList = m.getOrElse(stagingKey, List()) :+ a
      m + (stagingKey -> newList)
    },
    (stagingKey: StagingKey) => abilityStagingVar.updater { (m, a) =>
      val newList = m.getOrElse(stagingKey, List()).filter(_.key != a.key)
      m + (stagingKey -> newList)
    },
    (stagingKey: StagingKey) => abilityStagingVar.updater { (m, a) =>
      val currList = m.getOrElse(stagingKey, List())
      val currListKeys: Set[AbilityKey] = currList.map(_.key).toSet
      val newList =
        if (currListKeys.contains(a.key) && a.status != Status.Out) then
          currList.filter(_.key != a.key)
        else (currList :+ a)
      if a.inPool.runValidation(newList.collect { case ca: ChosenAbility =>
          ca
        })
      then m + (stagingKey -> newList)
      else m
    },
    (stagingKey: StagingKey, ability: AbilityTemplate) => abilityChoiceVar.updater { (acs, choice) =>
      val choices: Map[AbilityKey, ChosenAbility] =
        acs.getOrElse(stagingKey, Map())
      val mCurrChoice: Option[ChosenAbility] =
        choices.get(ability.key).headOption
      mCurrChoice.fold(acs) { currChoice =>
        acs + (stagingKey -> (choices + (ability.key -> currChoice.applyChoice(
          choice
        ))))
      }
    },
    (stagingKey: StagingKey, ability: AbilityTemplate) => abilityChoiceVar.updater { (acs, choice) =>
      val choices: Map[AbilityKey, ChosenAbility] =
        acs.getOrElse(stagingKey, Map())
      val mCurrChoice: Option[ChosenAbility] = choices.get(ability.key)
      mCurrChoice.fold(acs) { currChoice =>
        acs + (stagingKey -> (choices + (ability.key -> currChoice.removeChoice(
          choice
        ))))
      }
    },
    (key: StagingKey, direction: DieChange) => dieChangesVar.updater { (m, pq) =>
      val currForKey: Map[Quality | Power, DieChange] = m.getOrElse(key, Map())
      val changed: Option[DieChange] = currForKey.get(pq)
      val updated = CharacterComputation.DieChange.combine(changed, Some(direction))
      updated.fold(m + (key -> (currForKey - pq)))(dc =>
        m + (key -> (currForKey + (pq -> dc)))
      )
    },
    (key: StagingKey) => dieChangesVar.updater { (m, pq) =>
      val currForKey: Map[Quality | Power, DieChange] = m.getOrElse(key, Map())
      val changed: Option[DieChange] = currForKey.get(pq)
      val updated = CharacterComputation.DieChange.combine(changed, Some(CharacterComputation.DieChange.Upgrade))
      updated.fold(m + (key -> (currForKey - pq)))(dc =>
        m + (key -> (currForKey + (pq -> dc)))
      )
    },
    (key: StagingKey) => dieChangesVar.updater { (m, pq) =>
      val currForKey: Map[Quality | Power, DieChange] = m.getOrElse(key, Map())
      val changed: Option[DieChange] = currForKey.get(pq)
      val updated = CharacterComputation.DieChange.combine(changed, Some(CharacterComputation.DieChange.Downgrade))
      updated.fold(m + (key -> (currForKey - pq)))(dc =>
        m + (key -> (currForKey + (pq -> dc)))
      )
    },
    allDieChangesFunc,
    validationTrigger.map { _ =>
      val v = CharacterValidator(data)
      v.validBackground(data)
    },
    validationTrigger.map { _ =>
      val v = CharacterValidator(data)
      v.validPowerSource(data)
    },
    validationTrigger.map { _ =>
      val v = CharacterValidator(data)
      v.validArchetype(data)
    },
    validationTrigger.map { _ =>
      val v = CharacterValidator(data)
      v.validPersonality(data)
    },
    validationTrigger.map { _ =>
      val v = CharacterValidator(data)
      v.validRedAbilities(data)
    },
    healthVar.signal.map { _ =>
      val v = CharacterValidator(data)
      v.validHealth(data)
    },
    validationTrigger
      .combineWith(qualityStagingVar.signal, powerStagingVar.signal, abilityStagingVar.signal)
      .map { _ =>
        val e = CharacterExporter(data)
        e.exportData(data)
      }
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
      allDieChangesFunc
    )

    def allPowers: List[(Power, Die)] = CharacterComputation.computeAllPowers(
      powerStaging,
      background,
      powerSource,
      archetype,
      personality,
      dieChanges,
      allDieChangesFunc
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
    def baseAbilities: Map[StagingKey, Map[AbilityKey, ChosenAbility]] = baseAbilitiesParam

  // allDieChanges function for signalManager constructor
  private val allDieChangesFunc = (
      dcs: Map[StagingKey, Map[Quality | Power, DieChange]],
      mps: Option[PowerSource],
      mat: Option[Archetype],
      mpt: Option[Personality]
  ) => (mps.fold(List())(ps => dcs.get(ps).fold(List())(dcps => dcps.toList)) ++
      mat.fold(List())(at => dcs.get(at).fold(List())(dcat => dcat.toList)) ++
      mpt.fold(List())(pt =>
        dcs.get(pt).fold(List())(dcpt => dcpt.toList)
      )).toMap


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

  // Create lazy validator and exporter that work with current data
  private lazy val validator = CharacterValidator(data)
  private lazy val exporter = CharacterExporter(data)

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
