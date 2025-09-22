package scrpgHelper.chargen

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die
import scrpgHelper.status.Status
import scrpgHelper.chargen.characterModel.*


final class CharacterModel extends SignalManager:

  // Base data for initialization
  val basePersonalityQualities: Map[StagingKey, List[(Quality, Die)]] =
    Personality.personalities
      .map(p => p -> List((p.baseQuality, Die.d(8))))
      .toMap

  val powerSourceAbilities: List[(PowerSource, Map[AbilityKey, ChosenAbility])] =
    PowerSource.powerSources
      .map(ps =>
        ps ->
          (ps.abilityPools
            .flatMap(ap =>
              ap.abilities.map(a => a.key -> a.toChosenAbility(ap))
            )
            .toMap)
      )

  val archetypeAbilities: List[(Archetype, Map[AbilityKey, ChosenAbility])] =
    Archetype.archetypes
      .map(at =>
        at ->
          (at.abilityPools
            .flatMap(ap =>
              ap.abilities.map(a => a.key -> a.toChosenAbility(ap))
            )
            .toMap)
      )

  val personalityAbilities: List[(Personality, Map[AbilityKey, ChosenAbility])] =
    Personality.personalities
      .map(pt =>
        pt -> (pt.outAbilityPool.abilities
          .map(a => a.key -> a.toChosenAbility(pt.outAbilityPool))
          .toMap)
      )

  val redAbilities: List[(RedAbility.RedAbilityPhase, Map[AbilityKey, ChosenAbility])] =
    List(
      RedAbility.redAbilityPhase -> RedAbility.baseRedAbilityPool.abilities
        .map(a => a.key -> a.toChosenAbility(RedAbility.baseRedAbilityPool))
        .toMap
    )

  val baseAbilitiesVal: Map[StagingKey, Map[AbilityKey, ChosenAbility]] =
    (powerSourceAbilities ++ archetypeAbilities ++ personalityAbilities ++ redAbilities).toMap

  // Reactive state variables
  val backgroundVar: Var[Option[Background]] = Var(None)
  val powerSourceVar: Var[Option[PowerSource]] = Var(None)
  val archetypeVar: Var[Option[Archetype]] = Var(None)
  val personalityVar: Var[Option[Personality]] = Var(None)
  val healthVar: Var[Option[Int]] = Var(None)

  // Reactive staging vars
  val qualityStagingVar: Var[Map[StagingKey, List[(Quality, Die)]]] = Var(basePersonalityQualities)
  val powerStagingVar: Var[Map[StagingKey, List[(Power, Die)]]] = Var(Map())
  val abilityStagingVar: Var[Map[StagingKey, List[Ability[_]]]] = Var(Map())
  val abilityChoiceVar: Var[Map[StagingKey, Map[AbilityKey, ChosenAbility]]] = Var(baseAbilitiesVal)
  val dieChangesVar: Var[Map[StagingKey, Map[Quality | Power, DieChange]]] = Var(Map())

  // Change observers for core character selections
  val changeBackground: Observer[Background] = backgroundVar.updater { (_, b) => Some(b) }
  val changePowerSource: Observer[PowerSource] = powerSourceVar.updater { (_, ps) => Some(ps) }
  val changeArchetype: Observer[Archetype] = archetypeVar.updater { (_, at) => Some(at) }
  val changePersonality: Observer[Personality] = personalityVar.updater { (_, p) =>
    toggleAbility(p).onNext(p.ability)
    Some(p)
  }

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
      CharacterModel.this.allDieChanges
    )

    def allPowers: List[(Power, Die)] = CharacterComputation.computeAllPowers(
      powerStaging,
      background,
      powerSource,
      archetype,
      personality,
      dieChanges,
      CharacterModel.this.allDieChanges
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
    def baseAbilities: Map[StagingKey, Map[AbilityKey, ChosenAbility]] = baseAbilitiesVal

  // allDieChanges implementation for CharacterComputation
  def allDieChanges(
      dcs: Map[StagingKey, Map[Quality | Power, DieChange]],
      mps: Option[PowerSource],
      mat: Option[Archetype],
      mpt: Option[Personality]
  ): Map[Quality | Power, DieChange] =
    (mps.fold(List())(ps => dcs.get(ps).fold(List())(dcps => dcps.toList)) ++
      mat.fold(List())(at => dcs.get(at).fold(List())(dcat => dcat.toList)) ++
      mpt.fold(List())(pt =>
        dcs.get(pt).fold(List())(dcpt => dcpt.toList)
      )).toMap


  // SignalManager staging operations implementation
  def qualitiesSignal(
      stagingKey: Signal[Option[StagingKey]]
  ): Signal[List[(Quality, Die)]] =
    qualityStagingVar.signal
      .combineWith(stagingKey)
      .map((m, mb) => mb.flatMap(b => m.get(b)).getOrElse(List()))

  def addQuality(stagingKey: StagingKey): Observer[(Quality, Die)] =
    qualityStagingVar.updater { case (m, (q, d)) =>
      val newList = m.getOrElse(stagingKey, List()) :+ (q, d)
      if (newList == newList.distinct) {
        m + (stagingKey -> newList)
      } else {
        m
      }
    }
  def removeQuality(stagingKey: StagingKey): Observer[(Quality, Die)] =
    qualityStagingVar.updater { case (m, (q, d)) =>
      val newList = m.getOrElse(stagingKey, List()).filter(_._1 != q)
      m + (stagingKey -> newList)
    }

  def powersSignal(
      stagingKey: Signal[Option[StagingKey]]
  ): Signal[List[(Power, Die)]] =
    powerStagingVar.signal
      .combineWith(stagingKey)
      .map((m, mps) => mps.flatMap(ps => m.get(ps)).getOrElse(List()))

  def addPower(stagingKey: StagingKey): Observer[(Power, Die)] =
    powerStagingVar.updater { case (m, (p, d)) =>
      val newList = m.getOrElse(stagingKey, List()) :+ (p, d)
      m + (stagingKey -> newList)
    }
  def removePower(stagingKey: StagingKey): Observer[(Power, Die)] =
    powerStagingVar.updater { case (m, (p, d)) =>
      val newList = m.getOrElse(stagingKey, List()).filter(_._1 != p)
      m + (stagingKey -> newList)
    }

  def abilitiesSignal(
      stagingKey: Signal[Option[StagingKey]]
  ): Signal[List[Ability[_]]] =
    abilityStagingVar.signal
      .combineWith(stagingKey)
      .map((m, mb) => mb.flatMap(b => m.get(b)).getOrElse(List()))

  def addAbility(stagingKey: StagingKey): Observer[Ability[_]] =
    abilityStagingVar.updater { (m, a) =>
      val newList = m.getOrElse(stagingKey, List()) :+ a
      m + (stagingKey -> newList)
    }
  def removeAbility(stagingKey: StagingKey): Observer[Ability[_]] =
    abilityStagingVar.updater { (m, a) =>
      val newList = m.getOrElse(stagingKey, List()).filter(_.key != a.key)
      m + (stagingKey -> newList)
    }

  def abilitySelected(
      stagingKey: StagingKey,
      ability: Signal[Option[ChosenAbility]]
  ): Signal[Boolean] =
    abilityStagingVar.signal.combineWith(ability).map { (as, ma) =>
      val currListKeys = as.getOrElse(stagingKey, List()).map(_.key).toSet
      ma.fold(false)(a => currListKeys.contains(a.key))
    }

  def toggleAbility(stagingKey: StagingKey): Observer[ChosenAbility] =
    abilityStagingVar.updater { (m, a) =>
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
    }


  def abilityChoicesSignal(
      stagingKey: StagingKey
  ): Signal[Map[AbilityKey, ChosenAbility]] =
    abilityChoiceVar.signal.map(acs => acs.getOrElse(stagingKey, Map()))

  def addAbilityChoice(
      stagingKey: StagingKey,
      ability: AbilityTemplate
  ): Observer[AbilityChoice] =
    abilityChoiceVar.updater { (acs, choice) =>
      val choices: Map[AbilityKey, ChosenAbility] =
        acs.getOrElse(stagingKey, Map())
      val mCurrChoice: Option[ChosenAbility] =
        choices.get(ability.key).headOption
      mCurrChoice.fold(acs) { currChoice =>
        acs + (stagingKey -> (choices + (ability.key -> currChoice.applyChoice(
          choice
        ))))
      }
    }

  def removeAbilityChoice(
      stagingKey: StagingKey,
      ability: AbilityTemplate
  ): Observer[AbilityChoice] =
    abilityChoiceVar.updater { (acs, choice) =>
      val choices: Map[AbilityKey, ChosenAbility] =
        acs.getOrElse(stagingKey, Map())
      val mCurrChoice: Option[ChosenAbility] = choices.get(ability.key)
      mCurrChoice.fold(acs) { currChoice =>
        acs + (stagingKey -> (choices + (ability.key -> currChoice.removeChoice(
          choice
        ))))
      }
    }

  def changeDieChanges(
      key: StagingKey,
      direction: DieChange
  ): Observer[Quality | Power] = dieChangesVar
    .updater { (m, pq) =>
      val currForKey: Map[Quality | Power, DieChange] = m.getOrElse(key, Map())
      val changed: Option[DieChange] = currForKey.get(pq)
      val updated = CharacterComputation.DieChange.combine(changed, Some(direction))
      updated.fold(m + (key -> (currForKey - pq)))(dc =>
        m + (key -> (currForKey + (pq -> dc)))
      )
    }

  def upgrade(key: StagingKey): Observer[Quality | Power] =
    changeDieChanges(key, CharacterComputation.DieChange.Upgrade)

  def downgrade(key: StagingKey): Observer[Quality | Power] =
    changeDieChanges(key, CharacterComputation.DieChange.Downgrade)

  // SignalManager implementation - reactive signals that call pure CharacterData methods
  val allQualitiesSignal: Signal[List[(Quality, Die)]] = qualityStagingVar.signal
    .combineWith(
      backgroundVar.signal,
      powerSourceVar.signal,
      archetypeVar.signal,
      personalityVar.signal,
      dieChangesVar.signal
    )
    .map((_, _, _, _, _, _) => data.allQualities)

  val allPowersSignal: Signal[List[(Power, Die)]] = powerStagingVar.signal
    .combineWith(
      backgroundVar.signal,
      powerSourceVar.signal,
      archetypeVar.signal,
      personalityVar.signal,
      dieChangesVar.signal
    )
    .map((_, _, _, _, _, _) => data.allPowers)

  val allStagedAbilitiesSignal: Signal[List[ChosenAbility]] = abilityStagingVar.signal
    .combineWith(
      backgroundVar.signal,
      powerSourceVar.signal,
      archetypeVar.signal,
      personalityVar.signal
    )
    .map((_, _, _, _, _) => data.allStagedAbilities)

  val allChosenAbilitiesSignal: Signal[List[ChosenAbility]] = abilityChoiceVar.signal
    .combineWith(
      backgroundVar.signal,
      powerSourceVar.signal,
      archetypeVar.signal,
      personalityVar.signal
    )
    .map((_, _, _, _, _) => data.allChosenAbilities)

  val allPrinciplesSignal: Signal[List[Principle]] = abilityStagingVar.signal
    .combineWith(
      backgroundVar.signal,
      archetypeVar.signal
    )
    .map((_, _, _) => data.allPrinciples)

  val allAbilitiesSignal: Signal[List[Ability[_]]] =
    allStagedAbilitiesSignal
      .combineWith(allChosenAbilitiesSignal, allPrinciplesSignal)
      .map((_, _, _) => data.allAbilities)

  val redZoneHealthSignal: Signal[Option[Int]] =
    personalityVar.signal.map(_ => data.redZoneHealth)

  val powerQualityHealthSignal: Signal[Int] =
    allPowersSignal
      .combineWith(allQualitiesSignal)
      .combineWith(archetypeVar.signal)
      .combineWith(personalityVar.signal)
      .map((_, _, _, _) => data.powerQualityHealth)

  // Backward compatibility - provide signals without "Signal" suffix for existing code
  val allQualities: Signal[List[(Quality, Die)]] = allQualitiesSignal
  val allPowers: Signal[List[(Power, Die)]] = allPowersSignal
  val allStagedAbilities: Signal[List[ChosenAbility]] = allStagedAbilitiesSignal
  val allChosenAbilities: Signal[List[ChosenAbility]] = allChosenAbilitiesSignal
  val allPrinciples: Signal[List[Principle]] = allPrinciplesSignal
  val allAbilities: Signal[List[Ability[_]]] = allAbilitiesSignal
  val redZoneHealth: Signal[Option[Int]] = redZoneHealthSignal
  val powerQualityHealth: Signal[Int] = powerQualityHealthSignal


  val calcHealth: Observer[Int] =
    healthVar.updater { (m, n) =>
      if (m.isDefined) then m else Some(n)
    }

  // Create lazy validator and exporter that work with current data
  private val validator = CharacterValidator(data)
  private val exporter = CharacterExporter(data)

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

  // Create reactive validation signals that call pure validation functions when data changes
  val validBackground: Signal[Boolean] = validationTrigger.map { _ => validator.validBackground(data) }
  val validPowerSource: Signal[Boolean] = validationTrigger.map { _ => validator.validPowerSource(data) }
  val validArchetype: Signal[Boolean] = validationTrigger.map { _ => validator.validArchetype(data) }
  val validPersonality: Signal[Boolean] = validationTrigger.map { _ => validator.validPersonality(data) }
  val validRedAbilities: Signal[Boolean] = validationTrigger.map { _ => validator.validRedAbilities(data) }
  val validHealth: Signal[Boolean] = healthVar.signal.map { _ => validator.validHealth(data) }

  // Create reactive export signal that calls pure export function when data changes
  val forExport: Signal[CharacterModelExport] = validationTrigger
    .combineWith(allPowersSignal, allQualitiesSignal, allAbilitiesSignal)
    .map { _ => exporter.exportData(data) }
end CharacterModel
