package scrpgHelper.chargen

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die
import scrpgHelper.status.Status
import scrpgHelper.chargen.characterModel.*


final class CharacterModel extends CharacterState with CharacterStaging with CharacterExport with CharacterValidation with SignalManager:

  val background: Var[Option[Background]] = Var(None)
  val changeBackground: Observer[Background] = background.updater { (_, b) =>
    Some(b)
  }

  val powerSource: Var[Option[PowerSource]] = Var(None)
  val changePowerSource: Observer[PowerSource] =
    powerSource.updater { (_, ps) => Some(ps) }

  val archetype: Var[Option[Archetype]] = Var(None)
  val changeArchetype: Observer[Archetype] =
    archetype.updater { (_, at) => Some(at) }

  val personality: Var[Option[Personality]] = Var(None)
  val changePersonality: Observer[Personality] =
    personality.updater { (_, p) =>
      toggleAbility(p).onNext(p.ability)
      Some(p)
    }

  val health: Var[Option[Int]] = Var(None)

  // CharacterData implementation via delegation to computation object
  val data: CharacterData = new CharacterData:
    def background: Option[Background] = CharacterModel.this.background.now()
    def powerSource: Option[PowerSource] = CharacterModel.this.powerSource.now()
    def archetype: Option[Archetype] = CharacterModel.this.archetype.now()
    def personality: Option[Personality] = CharacterModel.this.personality.now()
    def health: Option[Int] = CharacterModel.this.health.now()

    def allQualities: List[(Quality, Die)] = CharacterComputation.computeAllQualities(
      qualityStaging.now(),
      background,
      powerSource,
      archetype,
      personality,
      dieChanges.now(),
      CharacterModel.this.allDieChanges
    )

    def allPowers: List[(Power, Die)] = CharacterComputation.computeAllPowers(
      powerStaging.now(),
      background,
      powerSource,
      archetype,
      personality,
      dieChanges.now(),
      CharacterModel.this.allDieChanges
    )

    def allStagedAbilities: List[ChosenAbility] = CharacterComputation.computeAllStagedAbilities(
      abilityStaging.now(),
      background,
      powerSource,
      archetype,
      personality
    )

    def allChosenAbilities: List[ChosenAbility] = CharacterComputation.computeAllChosenAbilities(
      abilityChoice.now(),
      background,
      powerSource,
      archetype,
      personality
    )

    def allPrinciples: List[Principle] = CharacterComputation.computeAllPrinciples(
      abilityStaging.now(),
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


  val basePersonalityQualities: Map[StagingKey, List[(Quality, Die)]] =
    Personality.personalities
      .map(p => p -> List((p.baseQuality, Die.d(8))))
      .toMap

  val qualityStaging: Var[Map[StagingKey, List[(Quality, Die)]]] = Var(
    basePersonalityQualities
  )
  def qualitiesSignal(
      stagingKey: Signal[Option[StagingKey]]
  ): Signal[List[(Quality, Die)]] =
    qualityStaging.signal
      .combineWith(stagingKey)
      .map((m, mb) => mb.flatMap(b => m.get(b)).getOrElse(List()))

  def addQuality(stagingKey: StagingKey): Observer[(Quality, Die)] =
    qualityStaging.updater { case (m, (q, d)) =>
      val newList = m.getOrElse(stagingKey, List()) :+ (q, d)
      if (newList == newList.distinct) {
        m + (stagingKey -> newList)
      } else {
        m
      }
    }
  def removeQuality(stagingKey: StagingKey): Observer[(Quality, Die)] =
    qualityStaging.updater { case (m, (q, d)) =>
      val newList = m.getOrElse(stagingKey, List()).filter(_._1 != q)
      m + (stagingKey -> newList)
    }

  val powerStaging: Var[Map[StagingKey, List[(Power, Die)]]] = Var(Map())
  def powersSignal(
      stagingKey: Signal[Option[StagingKey]]
  ): Signal[List[(Power, Die)]] =
    powerStaging.signal
      .combineWith(stagingKey)
      .map((m, mps) => mps.flatMap(ps => m.get(ps)).getOrElse(List()))

  def addPower(stagingKey: StagingKey): Observer[(Power, Die)] =
    powerStaging.updater { case (m, (p, d)) =>
      val newList = m.getOrElse(stagingKey, List()) :+ (p, d)
      m + (stagingKey -> newList)
    }
  def removePower(stagingKey: StagingKey): Observer[(Power, Die)] =
    powerStaging.updater { case (m, (p, d)) =>
      val newList = m.getOrElse(stagingKey, List()).filter(_._1 != p)
      m + (stagingKey -> newList)
    }

  val abilityStaging: Var[Map[StagingKey, List[Ability[_]]]] = Var(Map())
  def abilitiesSignal(
      stagingKey: Signal[Option[StagingKey]]
  ): Signal[List[Ability[_]]] =
    abilityStaging.signal
      .combineWith(stagingKey)
      .map((m, mb) => mb.flatMap(b => m.get(b)).getOrElse(List()))

  def addAbility(stagingKey: StagingKey): Observer[Ability[_]] =
    abilityStaging.updater { (m, a) =>
      val newList = m.getOrElse(stagingKey, List()) :+ a
      m + (stagingKey -> newList)
    }
  def removeAbility(stagingKey: StagingKey): Observer[Ability[_]] =
    abilityStaging.updater { (m, a) =>
      val newList = m.getOrElse(stagingKey, List()).filter(_.key != a.key)
      m + (stagingKey -> newList)
    }

  def abilitySelected(
      stagingKey: StagingKey,
      ability: Signal[Option[ChosenAbility]]
  ): Signal[Boolean] =
    abilityStaging.signal.combineWith(ability).map { (as, ma) =>
      val currListKeys = as.getOrElse(stagingKey, List()).map(_.key).toSet
      ma.fold(false)(a => currListKeys.contains(a.key))
    }

  def toggleAbility(stagingKey: StagingKey): Observer[ChosenAbility] =
    abilityStaging.updater { (m, a) =>
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

  val powerSourceAbilities
      : List[(PowerSource, Map[AbilityKey, ChosenAbility])] =
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

  val personalityAbilities
      : List[(Personality, Map[AbilityKey, ChosenAbility])] =
    Personality.personalities
      .map(pt =>
        pt -> (pt.outAbilityPool.abilities
          .map(a => a.key -> a.toChosenAbility(pt.outAbilityPool))
          .toMap)
      )

  val redAbilities: List[
    (RedAbility.RedAbilityPhase, Map[AbilityKey, ChosenAbility])
  ] =
    List(
      RedAbility.redAbilityPhase -> RedAbility.baseRedAbilityPool.abilities
        .map(a => a.key -> a.toChosenAbility(RedAbility.baseRedAbilityPool))
        .toMap
    )

  val baseAbilities: Map[StagingKey, Map[AbilityKey, ChosenAbility]] =
    (powerSourceAbilities ++ archetypeAbilities ++ personalityAbilities ++ redAbilities).toMap

  val abilityChoice: Var[Map[StagingKey, Map[AbilityKey, ChosenAbility]]] =
    Var(baseAbilities)
  def abilityChoicesSignal(
      stagingKey: StagingKey
  ): Signal[Map[AbilityKey, ChosenAbility]] =
    abilityChoice.signal.map(acs => acs.getOrElse(stagingKey, Map()))
  def addAbilityChoice(
      stagingKey: StagingKey,
      ability: AbilityTemplate
  ): Observer[AbilityChoice] =
    abilityChoice.updater { (acs, choice) =>
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
    abilityChoice.updater { (acs, choice) =>
      val choices: Map[AbilityKey, ChosenAbility] =
        acs.getOrElse(stagingKey, Map())
      val mCurrChoice: Option[ChosenAbility] = choices.get(ability.key)
      mCurrChoice.fold(acs) { currChoice =>
        acs + (stagingKey -> (choices + (ability.key -> currChoice.removeChoice(
          choice
        ))))
      }
    }


  val dieChanges: Var[Map[StagingKey, Map[Quality | Power, DieChange]]] = Var(
    Map()
  )
  def changeDieChanges(
      key: StagingKey,
      direction: DieChange
  ): Observer[Quality | Power] = dieChanges
    .updater { (m, pq) =>
      val currForKey: Map[Quality | Power, DieChange] = m.getOrElse(key, Map())
      val changed: Option[DieChange] = currForKey.get(pq)
      val updated = DieChange.combine(changed, Some(direction))
      updated.fold(m + (key -> (currForKey - pq)))(dc =>
        m + (key -> (currForKey + (pq -> dc)))
      )
    }
  def upgrade(key: StagingKey): Observer[Quality | Power] =
    changeDieChanges(key, DieChange.Upgrade)
  def downgrade(key: StagingKey): Observer[Quality | Power] =
    changeDieChanges(key, DieChange.Downgrade)

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

  val allQualities: Signal[List[(Quality, Die)]] = qualityStaging.signal
    .combineWith(
      background.signal,
      powerSource.signal,
      archetype.signal,
      personality.signal,
      dieChanges
    )
    .map((_, _, _, _, _, _) => data.allQualities)

  val allPowers: Signal[List[(Power, Die)]] = powerStaging.signal
    .combineWith(
      background.signal,
      powerSource.signal,
      archetype.signal,
      personality.signal,
      dieChanges
    )
    .map((_, _, _, _, _, _) => data.allPowers)

  val allStagedAbilities: Signal[List[ChosenAbility]] = abilityStaging.signal
    .combineWith(
      background.signal,
      powerSource.signal,
      archetype.signal,
      personality.signal
    )
    .map((_, _, _, _, _) => data.allStagedAbilities)

  val allChosenAbilities: Signal[List[ChosenAbility]] = abilityChoice.signal
    .combineWith(
      background.signal,
      powerSource.signal,
      archetype.signal,
      personality.signal
    )
    .map((_, _, _, _, _) => data.allChosenAbilities)

  val allPrinciples: Signal[List[Principle]] = abilityStaging.signal
    .combineWith(
      background.signal,
      archetype.signal
    )
    .map((_, _, _) => data.allPrinciples)

  val allAbilities: Signal[List[Ability[_]]] =
    allStagedAbilities
      .combineWith(allChosenAbilities, allPrinciples)
      .map((_, _, _) => data.allAbilities)

  val redZoneHealth: Signal[Option[Int]] =
    personality.signal.map(_ => data.redZoneHealth)

  val powerQualityHealth: Signal[Int] =
    allPowers
      .combineWith(allQualities)
      .combineWith(archetype.signal)
      .combineWith(personality.signal)
      .map((_, _, _, _) => data.powerQualityHealth)

  val calcHealth: Observer[Int] =
    health.updater { (m, n) =>
      if (m.isDefined) then m else Some(n)
    }

  private val validator = CharacterValidator(this, this)
  val validBackground: Signal[Boolean] = validator.validBackground
  val validPowerSource: Signal[Boolean] = validator.validPowerSource
  val validArchetype: Signal[Boolean] = validator.validArchetype
  val validPersonality: Signal[Boolean] = validator.validPersonality
  val validRedAbilities: Signal[Boolean] = validator.validRedAbilities
  val validHealth: Signal[Boolean] = validator.validHealth

  private val exporter = CharacterExporter(this)
  val forExport: Signal[CharacterModelExport] = exporter.forExport
end CharacterModel
