package scrpgHelper.chargen

import scala.scalajs.js
import scala.scalajs.js.annotation.*

import org.scalajs.dom

import com.raquo.laminar.api.L.{*, given}

import scrpgHelper.rolls.Die

import scrpgHelper.status.Status

case class CharacterModelExport(
  background: Option[String],
  powerSource: Option[String],
  archetype: Option[String],
  personality: Option[String],
  health: Option[Int],
  powers: List[(Power, Die)],
  qualities: List[(Quality, Die)],
  abilities: List[ChosenAbility],
  principles: List[Principle],
):
    def render: String =
      val healthMarks = health.map(Health.calcRanges(_))
      val powerText = powers.map((p, d) => s"* ${p.name}, ${p.category.toString} - ${d.toString}").mkString("\n\t")
      val qualityText = qualities.map((q, d) => s"* ${q.name}, ${q.category.toString} - ${d.toString}").mkString("\n\t")
      val principleText = principles.map(p => "* " + RenderAbility.renderPrincipleText(p)).mkString("\n\t")
      val abilityText = abilities.map(ca => "* " + RenderAbility.renderChosenAbilityText(ca)).mkString("\n\t")
      s"""
Background: ${background.getOrElse("<none>")}
PowerSource: ${powerSource.getOrElse("<none>")}
Archetype: ${archetype.getOrElse("<none>")}
Personality: ${personality.getOrElse("<none>")}
Health: ${healthMarks.fold("")(hm => hm._1.toString + " - " + (hm._2 + 1).toString + "; " + hm._2.toString + " - " + (hm._3 + 1).toString + "; " + hm._3 + " - 1")}
Powers:
\t${powerText}
Qualities:
\t${qualityText}
Principles:
\t${principleText}
Abilities:
\t${abilityText}
       """
    end render
end CharacterModelExport

final class CharacterModel:
  val background: Var[Option[Background]] = Var(None)
  val backgroundSignal = background.signal
  val changeBackground: Observer[Background] = background.updater { (_, b) =>
    Some(b)
  }

  val powerSource: Var[Option[PowerSource]] = Var(None)
  val powerSourceSignal = powerSource.signal
  val changePowerSource: Observer[PowerSource] =
    powerSource.updater { (_, ps) => Some(ps) }

  val archetype: Var[Option[Archetype]] = Var(None)
  val archetypeSignal = archetype.signal
  val changeArchetype: Observer[Archetype] =
    archetype.updater { (_, at) => Some(at) }

  val personality: Var[Option[Personality]] = Var(None)
  val personalitySignal = personality.signal
  val changePersonality: Observer[Personality] =
    personality.updater { (_, p) =>
      toggleAbility(p).onNext(p.ability)
      Some(p)
    }

  val health: Var[Option[Int]] = Var(None)
  val healthSignal = health.signal

  type StagingKey = Background | PowerSource | Archetype | Personality |
    RedAbility.RedAbilityPhase

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

  def abilitySelected(stagingKey: StagingKey, ability: Signal[Option[ChosenAbility]]): Signal[Boolean] =
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

  val allQualities: Signal[List[(Quality, Die)]] = qualityStaging.signal
    .combineWith(
      backgroundSignal,
      powerSourceSignal,
      archetypeSignal,
      personalitySignal
    )
    .map((quals, mbg, mps, mat, mpt) =>
      mbg.fold(List())(bg => quals.getOrElse(bg, List())) ++
        mps.fold(List())(ps => quals.getOrElse(ps, List())) ++
        mat.fold(List())(at => quals.getOrElse(at, List())) ++
        mpt.fold(List())(pt => quals.getOrElse(pt, List()))
    )

  val allPowers: Signal[List[(Power, Die)]] = powerStaging.signal
    .combineWith(
      backgroundSignal,
      powerSourceSignal,
      archetypeSignal,
      personalitySignal
    )
    .map((pows, mbg, mps, mat, mpt) =>
      mbg.fold(List())(bg => pows.getOrElse(bg, List())) ++
        mps.fold(List())(ps => pows.getOrElse(ps, List())) ++
        mat.fold(List())(at => pows.getOrElse(at, List())) ++
        mpt.fold(List())(pt => pows.getOrElse(pt, List()))
    )

  val allStagedAbilities: Signal[List[ChosenAbility]] = abilityStaging.signal
    .combineWith(
      backgroundSignal,
      powerSourceSignal,
      archetypeSignal,
      personalitySignal
    )
    .map((abils, mbg, mps, mat, mpt) =>
      mbg.fold(List())(bg => abils.getOrElse(bg, List())) ++
        mps.fold(List())(ps => abils.getOrElse(ps, List())) ++
        mat.fold(List())(at => abils.getOrElse(at, List())) ++
        mpt.fold(List())(pt => abils.getOrElse(pt, List())) ++
        abils.getOrElse(RedAbility.redAbilityPhase, List())
    )
    .map(_.collect { case ca: ChosenAbility => ca })

  val allChosenAbilities: Signal[List[ChosenAbility]] = abilityChoice.signal
    .combineWith(
      backgroundSignal,
      powerSourceSignal,
      archetypeSignal,
      personalitySignal
    )
    .map((abils, mbg, mps, mat, mpt) =>
      mbg.fold(List())(bg => abils.getOrElse(bg, List())) ++
        mps.fold(List())(ps => abils.getOrElse(ps, List())) ++
        mat.fold(List())(at => abils.getOrElse(at, List())) ++
        mpt.fold(List())(pt => abils.getOrElse(pt, List())) ++
        abils.getOrElse(RedAbility.redAbilityPhase, List())
    )
    .map(_.map(_._2).filter(_.descriptionFilledOut).toList)

  val allPrinciples: Signal[List[Principle]] = abilityStaging.signal
    .combineWith(
      backgroundSignal,
      archetypeSignal,
    )
    .map((abils, mbg, mat) =>
      mbg.fold(List())(bg => abils.getOrElse(bg, List())) ++
        mat.fold(List())(at => abils.getOrElse(at, List()))
    )
    .map(_.collect { case p: Principle => p })

  val allAbilities: Signal[List[Ability[_]]] =
    allStagedAbilities
      .combineWith(allChosenAbilities, allPrinciples)
      .map { (asa, aca, aps) =>
        val abilityIds = asa.map(_.key).toSet
        aca.filter(a => abilityIds.contains(a.key)) ++ aps
      }

  val redZoneHealth: Signal[Option[Int]] =
    personalitySignal.map(_.flatMap(p => p.statusDice.get(Status.Red).map(_.n)))

  val powerQualityHealth: Signal[Int] =
    allPowers.combineWith(allQualities).map { (ps, qs) =>
            val athleticRolls = ps.filter(_._1.category == PowerCategory.Athletic).map(_._2.n)
            val maxAthletic = if athleticRolls.isEmpty then 4 else athleticRolls.max
            val mentalRolls = qs.filter(_._1.category == QualityCategory.Mental).map(_._2.n)
            val maxMental = if mentalRolls.isEmpty then 4 else mentalRolls.max
            List(maxAthletic, maxMental).max
          }

  val calcHealth: Observer[Int] =
    health.updater { (m, n) =>
      if(m.isDefined) then m else Some(n)
    }

  val validBackground: Signal[Boolean] = backgroundSignal
    .combineWith(qualityStaging.signal, abilityStaging.signal)
    .map { (mb, qm, am) =>
      mb.fold(false)(b =>
        b.valid(qm.getOrElse(b, List()), am.getOrElse(b, List()))
      )
    }

  val validPowerSource: Signal[Boolean] = powerSourceSignal
    .combineWith(
      backgroundSignal.map((mb: Option[Background]) =>
        mb.toList.flatMap((b: Background) => b.powerSourceDice)
      ),
      powerStaging.signal,
      qualityStaging.signal,
      abilityStaging.signal,
      abilityChoice.signal
    )
    .map { (mp, dice, pm, qm, asm, am) =>
      mp.fold(false) { p =>
        val powers: List[Power] = pm.getOrElse(p, List()).map(_._1)
        val qualities: List[Quality] = qm.getOrElse(p, List()).map(_._1)
        val selectedAbilities: Set[AbilityKey] = asm
          .getOrElse(p, List())
          .collect { case ca: ChosenAbility => ca }
          .map(_.key)
          .toSet
        val abilityMap: Map[AbilityKey, ChosenAbility] =
          am.getOrElse(p, Map())
        val abilities: List[ChosenAbility] =
          abilityMap.values.toList
            .filter(a => selectedAbilities.contains(a.key))
        p.valid(dice, powers, qualities, abilities)
      }
    }

  val validArchetype: Signal[Boolean] = archetypeSignal
    .combineWith(
      powerSourceSignal.map(_.toList.flatMap(_.archetypeDiePool)),
      powerStaging.signal,
      qualityStaging.signal,
      abilityStaging.signal,
      abilityChoice.signal,
      backgroundSignal,
      powerSourceSignal
    )
    .map { (mat, dice, pm, qm, asm, am, mbg, mps) =>
      mat.fold(false) { at =>
        val powers: List[Power] = pm.getOrElse(at, List()).map(_._1)
        val allPowers: List[Power] =
          mps.fold(List())(ps => pm.getOrElse(ps, List()).map(_._1)) ++ powers
        val qualities: List[Quality] = qm.getOrElse(at, List()).map(_._1)
        val allQualities: List[Quality] =
          mbg.fold(List())(bg => qm.getOrElse(bg, List()).map(_._1)) ++ mps
            .fold(List())(ps => qm.getOrElse(ps, List()).map(_._1)) ++ qualities
        val selectedAbilities: Set[AbilityKey] = asm
          .getOrElse(at, List())
          .collect { case ca: ChosenAbility => ca }
          .map(_.key)
          .toSet
        val abilityMap: Map[AbilityKey, ChosenAbility] =
          am.getOrElse(at, Map())
        val abilities: List[ChosenAbility] =
          abilityMap.values.toList
            .filter(a => selectedAbilities.contains(a.key))
        at.valid(dice, powers, qualities, abilities, allPowers, allQualities)
      }
    }

  val validPersonality: Signal[Boolean] = personalitySignal
    .combineWith(
      powerStaging.signal,
      qualityStaging.signal,
      abilityStaging.signal,
      abilityChoice.signal
    )
    .map { (mp, pm, qm, asm, am) =>
      mp.fold(false) { p =>
        val qualities: List[Quality] = qm.getOrElse(p, List()).map(_._1)
        val selectedAbilities: Set[AbilityKey] = asm
          .getOrElse(p, List())
          .collect { case ca: ChosenAbility => ca }
          .map(_.key)
          .toSet
        val abilityMap: Map[AbilityKey, ChosenAbility] =
          am.getOrElse(p, Map())
        val abilities: List[ChosenAbility] =
          abilityMap.values.toList
            .filter(a => selectedAbilities.contains(a.key))
        p.valid(qualities, abilities)
      }
    }

  val validRedAbilities: Signal[Boolean] =
    abilityStaging.signal.combineWith(abilityChoice.signal).map { (as, am) =>
      val selectedAbilities: Set[AbilityKey] = as
        .getOrElse(RedAbility.redAbilityPhase, List())
        .collect { case ca: ChosenAbility => ca }
        .map(_.key)
        .toSet
      val abilityMap: Map[AbilityKey, ChosenAbility] =
        am.getOrElse(RedAbility.redAbilityPhase, Map())
      val redAbilities: List[ChosenAbility] =
        abilityMap.values.toList.filter(a => selectedAbilities.contains(a.key))
      redAbilities.size == RedAbility.baseRedAbilityPool.max &&
      redAbilities.map(_.valid).foldLeft(true)(_ && _)
    }

  val validHealth: Signal[Boolean] = healthSignal.map(_.isDefined)

  val forExport: Signal[CharacterModelExport] =
    backgroundSignal.combineWith(
      powerSourceSignal,
      archetypeSignal,
      personalitySignal,
      healthSignal,
      allPowers,
      allQualities,
      allAbilities
    ).map { (bg, ps, at, pt, h, pows, quals, abils) =>
      CharacterModelExport(
        bg.map(_.name),
        ps.map(_.name),
        at.map(_.name),
        pt.map(_.name),
        h,
        pows,
        quals,
        abils.collect { case ca: ChosenAbility => ca },
        abils.collect { case p: Principle => p },
      )
    }
end CharacterModel
