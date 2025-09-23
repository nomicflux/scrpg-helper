package scrpgHelper.chargen.characterModel

import com.raquo.laminar.api.L.{*, given}
import scrpgHelper.rolls.Die
import scrpgHelper.status.Status
import scrpgHelper.chargen.*

/** Pure reactive signal management for character data.
  *
  * Handles all reactive signals and computed values derived from character state.
  * Provides the reactive interface while delegating business logic to pure functions.
  *
  * Simplified constructor - calculates all derived data internally.
  */
class CharacterSignalManager(
    // Core reactive state
    backgroundVar: Var[Option[Background]],
    powerSourceVar: Var[Option[PowerSource]],
    archetypeVar: Var[Option[Archetype]],
    personalityVar: Var[Option[Personality]],
    healthVar: Var[Option[Int]],

    // Staging state
    val qualityStagingVar: Var[Map[CharacterComputation.StagingKey, List[(Quality, Die)]]],
    val powerStagingVar: Var[Map[CharacterComputation.StagingKey, List[(Power, Die)]]],
    val abilityStagingVar: Var[Map[CharacterComputation.StagingKey, List[Ability[_]]]],
    val abilityChoiceVar: Var[Map[CharacterComputation.StagingKey, Map[AbilityKey, ChosenAbility]]],
    val dieChangesVar: Var[Map[CharacterComputation.StagingKey, Map[Quality | Power, CharacterComputation.DieChange]]],

    // Data access
    dataProvider: () => CharacterData
) extends SignalManager:

  // Internal static data calculations
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

  // Internal observer creation
  private val changeBackgroundVal = backgroundVar.updater { (_, b: Background) => Some(b) }
  private val changePowerSourceVal = powerSourceVar.updater { (_, ps: PowerSource) => Some(ps) }
  private val changeArchetypeVal = archetypeVar.updater { (_, at: Archetype) => Some(at) }
  private val changePersonalityVal = personalityVar.updater { (_, p: Personality) => Some(p) }
  private val calcHealthVal = healthVar.updater { (m, n: Int) => if (m.isDefined) then m else Some(n) }

  // Focused validation triggers - only depend on what each validation actually needs
  private val backgroundValidationTrigger: Signal[Unit] = backgroundVar.signal
    .combineWith(qualityStagingVar.signal, abilityStagingVar.signal)
    .map { _ => () }

  private val powerSourceValidationTrigger: Signal[Unit] = powerSourceVar.signal
    .combineWith(
      backgroundVar.signal,
      powerStagingVar.signal,
      qualityStagingVar.signal,
      abilityStagingVar.signal,
      abilityChoiceVar.signal
    )
    .map { _ => () }

  private val archetypeValidationTrigger: Signal[Unit] = archetypeVar.signal
    .combineWith(
      powerSourceVar.signal,
      backgroundVar.signal,
      powerStagingVar.signal,
      qualityStagingVar.signal,
      abilityStagingVar.signal,
      abilityChoiceVar.signal
    )
    .map { _ => () }

  private val personalityValidationTrigger: Signal[Unit] = personalityVar.signal
    .combineWith(
      qualityStagingVar.signal,
      abilityStagingVar.signal,
      abilityChoiceVar.signal
    )
    .map { _ => () }

  private val redAbilitiesValidationTrigger: Signal[Unit] = abilityStagingVar.signal
    .combineWith(abilityChoiceVar.signal)
    .map { _ => () }

  // Export trigger needs all data
  private val exportTrigger: Signal[Unit] = backgroundVar.signal
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

  // Internal validation and export signals
  private val validBackgroundVal: Signal[Boolean] = backgroundValidationTrigger.map { _ =>
    val data = dataProvider()
    val v = CharacterValidator(data)
    v.validBackground(data)
  }
  private val validPowerSourceVal: Signal[Boolean] = powerSourceValidationTrigger.map { _ =>
    val data = dataProvider()
    val v = CharacterValidator(data)
    v.validPowerSource(data)
  }
  private val validArchetypeVal: Signal[Boolean] = archetypeValidationTrigger.map { _ =>
    val data = dataProvider()
    val v = CharacterValidator(data)
    v.validArchetype(data)
  }
  private val validPersonalityVal: Signal[Boolean] = personalityValidationTrigger.map { _ =>
    val data = dataProvider()
    val v = CharacterValidator(data)
    v.validPersonality(data)
  }
  private val validRedAbilitiesVal: Signal[Boolean] = redAbilitiesValidationTrigger.map { _ =>
    val data = dataProvider()
    val v = CharacterValidator(data)
    v.validRedAbilities(data)
  }
  private val validHealthVal: Signal[Boolean] = healthVar.signal.map { _ =>
    val data = dataProvider()
    val v = CharacterValidator(data)
    v.validHealth(data)
  }
  private val forExportVal: Signal[CharacterModelExport] = exportTrigger.map { _ =>
    val data = dataProvider()
    val e = CharacterExporter(data)
    e.exportData(data)
  }

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

  // Internal observer function implementations
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

  def addAbilityChoice(stagingKey: StagingKey, ability: AbilityTemplate): Observer[AbilityChoice] =
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

  def removeAbilityChoice(stagingKey: StagingKey, ability: AbilityTemplate): Observer[AbilityChoice] =
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

  def changeDieChanges(key: StagingKey, direction: DieChange): Observer[Quality | Power] =
    dieChangesVar.updater { (m, pq) =>
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

  // Interface implementation - exposing internal computed values
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
      .distinctBy { case (m, mb) => mb.flatMap(b => m.get(b)) }
      .map((m, mb) => mb.flatMap(b => m.get(b)).getOrElse(List()))

  def powersSignal(
      stagingKey: Signal[Option[CharacterComputation.StagingKey]]
  ): Signal[List[(Power, Die)]] =
    powerStagingVar.signal
      .combineWith(stagingKey)
      .distinctBy { case (m, mps) => mps.flatMap(ps => m.get(ps)) }
      .map((m, mps) => mps.flatMap(ps => m.get(ps)).getOrElse(List()))

  def abilitiesSignal(
      stagingKey: Signal[Option[CharacterComputation.StagingKey]]
  ): Signal[List[Ability[_]]] =
    abilityStagingVar.signal
      .combineWith(stagingKey)
      .distinctBy { case (m, mb) => mb.flatMap(b => m.get(b)) }
      .map((m, mb) => mb.flatMap(b => m.get(b)).getOrElse(List()))

  def abilitySelected(
      stagingKey: CharacterComputation.StagingKey,
      ability: Signal[Option[ChosenAbility]]
  ): Signal[Boolean] =
    abilityStagingVar.signal
      .distinctBy(_.getOrElse(stagingKey, List()))
      .combineWith(ability).map { (as, ma) =>
      val currListKeys = as.getOrElse(stagingKey, List()).map(_.key).toSet
      ma.fold(false)(a => currListKeys.contains(a.key))
    }

  def abilityChoicesSignal(
      stagingKey: CharacterComputation.StagingKey
  ): Signal[Map[AbilityKey, ChosenAbility]] =
    abilityChoiceVar.signal
      .distinctBy(_.getOrElse(stagingKey, Map()))
      .map(acs => acs.getOrElse(stagingKey, Map()))

  // SignalManager implementation - using focused optics for efficient data access
  val allQualitiesSignal: Signal[List[(Quality, Die)]] = qualityStagingVar.signal
    .combineWith(
      backgroundVar.signal,
      powerSourceVar.signal,
      archetypeVar.signal,
      personalityVar.signal,
      dieChangesVar.signal
    )
    .map((qualityStaging, background, powerSource, archetype, personality, dieChanges) => {
      val relDieChanges = allDieChanges(dieChanges, powerSource, archetype, personality)

      val bgQualities = background.map(bg => CharacterData.qualitiesFromStaging(bg, relDieChanges).get(qualityStaging)).getOrElse(List())
      val psQualities = powerSource.map(ps => CharacterData.qualitiesFromStaging(ps, relDieChanges).get(qualityStaging)).getOrElse(List())
      val atQualities = archetype.map(at => CharacterData.qualitiesFromStaging(at, relDieChanges).get(qualityStaging)).getOrElse(List())
      val ptQualities = personality.map(pt => CharacterData.qualitiesFromStaging(pt, relDieChanges).get(qualityStaging)).getOrElse(List())

      bgQualities ++ psQualities ++ atQualities ++ ptQualities
    })

  val allPowersSignal: Signal[List[(Power, Die)]] = powerStagingVar.signal
    .combineWith(
      backgroundVar.signal,
      powerSourceVar.signal,
      archetypeVar.signal,
      personalityVar.signal,
      dieChangesVar.signal
    )
    .map((powerStaging, background, powerSource, archetype, personality, dieChanges) => {
      val relDieChanges = allDieChanges(dieChanges, powerSource, archetype, personality)

      val bgPowers = background.map(bg => CharacterData.powersFromStaging(bg, relDieChanges).get(powerStaging)).getOrElse(List())
      val psPowers = powerSource.map(ps => CharacterData.powersFromStaging(ps, relDieChanges).get(powerStaging)).getOrElse(List())
      val atPowers = archetype.map(at => CharacterData.powersFromStaging(at, relDieChanges).get(powerStaging)).getOrElse(List())
      val ptPowers = personality.map(pt => CharacterData.powersFromStaging(pt, relDieChanges).get(powerStaging)).getOrElse(List())

      bgPowers ++ psPowers ++ atPowers ++ ptPowers
    })

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
      .map((staged, chosen, principles) =>
        CharacterComputation.computeAllAbilities(staged, chosen, principles))

  val redZoneHealthSignal: Signal[Option[Int]] =
    personalityVar.signal.map(_ => dataProvider().redZoneHealth)

  val powerQualityHealthSignal: Signal[Int] =
    allPowersSignal
      .combineWith(allQualitiesSignal)
      .combineWith(archetypeVar.signal)
      .combineWith(personalityVar.signal)
      .map((powers, qualities, archetype, personality) =>
        CharacterComputation.computePowerQualityHealth(powers, qualities, archetype, personality))

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