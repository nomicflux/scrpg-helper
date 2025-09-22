package scrpgHelper.chargen

import munit.FunSuite
import scrpgHelper.rolls.Die
import scrpgHelper.status.Status
import scrpgHelper.chargen.characterModel.*
import scrpgHelper.chargen.powers.Accident
import scrpgHelper.chargen.archetypes.Gadgeteer

class CharacterComputationSpec extends FunSuite:

  test("DieChange enum operations"):
    val upgrade = CharacterComputation.DieChange.Upgrade
    val downgrade = CharacterComputation.DieChange.Downgrade

    // Test onDie operations
    assertEquals(upgrade.onDie(Die.d(6)), Die.d(8), "Upgrade should increase die size")
    assertEquals(downgrade.onDie(Die.d(8)), Die.d(6), "Downgrade should decrease die size")

  test("DieChange.combine operations"):
    import CharacterComputation.DieChange

    // Test combination logic
    assertEquals(DieChange.combine(None, None), None, "None + None = None")
    assertEquals(DieChange.combine(Some(DieChange.Upgrade), None), Some(DieChange.Upgrade), "Upgrade + None = Upgrade")
    assertEquals(DieChange.combine(None, Some(DieChange.Downgrade)), Some(DieChange.Downgrade), "None + Downgrade = Downgrade")
    assertEquals(DieChange.combine(Some(DieChange.Upgrade), Some(DieChange.Upgrade)), Some(DieChange.Upgrade), "Upgrade + Upgrade = Upgrade")
    assertEquals(DieChange.combine(Some(DieChange.Upgrade), Some(DieChange.Downgrade)), None, "Upgrade + Downgrade = None (cancel out)")

  test("computeAllQualities with empty data"):
    val result = CharacterComputation.computeAllQualities(
      qualityStaging = Map(),
      background = None,
      powerSource = None,
      archetype = None,
      personality = None,
      dieChanges = Map(),
      allDieChangesFunction = (_, _, _, _) => Map()
    )
    assertEquals(result, List(), "Empty data should return empty list")

  test("computeAllQualities with background data"):
    val bg = Background.created
    val qualities = List((Quality.alertness, Die.d(6)), (Quality.conviction, Die.d(8)))
    val qualityStaging: Map[CharacterComputation.StagingKey, List[(Quality, Die)]] = Map(bg -> qualities)

    val result = CharacterComputation.computeAllQualities(
      qualityStaging = qualityStaging,
      background = Some(bg),
      powerSource = None,
      archetype = None,
      personality = None,
      dieChanges = Map(),
      allDieChangesFunction = (_, _, _, _) => Map()
    )
    assertEquals(result, qualities, "Should return background qualities")

  test("computeAllQualities with die changes"):
    val bg = Background.created
    val baseQualities = List((Quality.alertness, Die.d(6)))
    val qualityStaging: Map[CharacterComputation.StagingKey, List[(Quality, Die)]] = Map(bg -> baseQualities)
    val dieChanges: Map[CharacterComputation.StagingKey, Map[Quality | Power, CharacterComputation.DieChange]] =
      Map(bg -> Map(Quality.alertness -> CharacterComputation.DieChange.Upgrade))

    val result = CharacterComputation.computeAllQualities(
      qualityStaging = qualityStaging,
      background = Some(bg),
      powerSource = None,
      archetype = None,
      personality = None,
      dieChanges = dieChanges,
      allDieChangesFunction = (dc, _, _, _) => dc.getOrElse(bg, Map())
    )

    assertEquals(result, List((Quality.alertness, Die.d(8))), "Die changes should be applied")

  test("computeAllPowers with power source data"):
    val ps = Accident.accident
    val powers = List((ps.powerList.head, Die.d(6)))
    val powerStaging: Map[CharacterComputation.StagingKey, List[(Power, Die)]] = Map(ps -> powers)

    val result = CharacterComputation.computeAllPowers(
      powerStaging = powerStaging,
      background = None,
      powerSource = Some(ps),
      archetype = None,
      personality = None,
      dieChanges = Map(),
      allDieChangesFunction = (_, _, _, _) => Map()
    )
    assertEquals(result, powers, "Should return power source powers")

  test("computeAllStagedAbilities with chosen abilities"):
    val bg = Background.created
    val testTemplate = AbilityTemplate(
      new AbilityId(),
      "Test Ability",
      Status.Green,
      AbilityCategory.Action,
      _ => List(),
      List("Test")
    )
    val testPool = AbilityPool(1, List(testTemplate))
    val testAbility = ChosenAbility(testTemplate, testPool, List())
    val abilityStaging: Map[CharacterComputation.StagingKey, List[Ability[_]]] = Map(bg -> List(testAbility))

    val result = CharacterComputation.computeAllStagedAbilities(
      abilityStaging = abilityStaging,
      background = Some(bg),
      powerSource = None,
      archetype = None,
      personality = None
    )
    assertEquals(result, List(testAbility), "Should return staged chosen abilities")

  test("computeAllStagedAbilities filters out non-ChosenAbility"):
    val bg = Background.created
    val principle = Principle.of("Test Principle", PrincipleCategory.Esoteric)
    val testTemplate = AbilityTemplate(
      new AbilityId(),
      "Test Ability",
      Status.Green,
      AbilityCategory.Action,
      _ => List(),
      List("Test")
    )
    val testPool = AbilityPool(1, List(testTemplate))
    val testAbility = ChosenAbility(testTemplate, testPool, List())
    val abilityStaging: Map[CharacterComputation.StagingKey, List[Ability[_]]] = Map(bg -> List(principle, testAbility))

    val result = CharacterComputation.computeAllStagedAbilities(
      abilityStaging = abilityStaging,
      background = Some(bg),
      powerSource = None,
      archetype = None,
      personality = None
    )
    assertEquals(result, List(testAbility), "Should filter out Principles, keep only ChosenAbility")

  test("computeAllChosenAbilities returns empty with no data"):
    val result = CharacterComputation.computeAllChosenAbilities(
      abilityChoice = Map(),
      background = None,
      powerSource = None,
      archetype = None,
      personality = None
    )
    assertEquals(result, List(), "Should return empty list with no data")

  test("computeAllPrinciples returns empty with no data"):
    val result = CharacterComputation.computeAllPrinciples(
      abilityStaging = Map(),
      background = None,
      archetype = None
    )
    assertEquals(result, List(), "Should return empty list with no data")

  test("computeAllAbilities returns empty with no data"):
    val result = CharacterComputation.computeAllAbilities(
      allStagedAbilities = List(),
      allChosenAbilities = List(),
      allPrinciples = List()
    )
    assertEquals(result, List(), "Should return empty list with no data")

  test("computeRedZoneHealth with personality"):
    val personality = Personality.loneWolf
    val result = CharacterComputation.computeRedZoneHealth(Some(personality))

    // Verify it extracts red die value from personality status dice
    val expected = personality.statusDice.get(Status.Red).map(_.n)
    assertEquals(result, expected, "Should return red zone health from personality")

  test("computeRedZoneHealth without personality"):
    val result = CharacterComputation.computeRedZoneHealth(None)
    assertEquals(result, None, "Should return None when no personality")

  test("computePowerQualityHealth with athletic powers and mental qualities"):
    val athleticPower = Power("Athletic Power", PowerCategory.Athletic)
    val mentalQuality = Quality("Mental Quality", QualityCategory.Mental)
    val allPowers = List((athleticPower, Die.d(10)))
    val allQualities = List((mentalQuality, Die.d(8)))

    val result = CharacterComputation.computePowerQualityHealth(
      allPowers = allPowers,
      allQualities = allQualities,
      archetype = None,
      personality = None
    )
    assertEquals(result, 10, "Should return max of athletic power (10) and mental quality (8)")

  test("computePowerQualityHealth with empty lists defaults to 4"):
    val result = CharacterComputation.computePowerQualityHealth(
      allPowers = List(),
      allQualities = List(),
      archetype = None,
      personality = None
    )
    assertEquals(result, 4, "Should default to 4 when no applicable powers or qualities")

  test("computePowerQualityHealth with archetype extra health categories"):
    val archetype = Gadgeteer.gadgeteer
    val physicalQuality = Quality("Physical Quality", QualityCategory.Physical)
    val allQualities = List((physicalQuality, Die.d(12)))

    val result = CharacterComputation.computePowerQualityHealth(
      allPowers = List(),
      allQualities = allQualities,
      archetype = Some(archetype),
      personality = None
    )

    // Should use physical quality if archetype allows it, or default to 4
    val expectedMax = if (archetype.extraHealthCategories.contains(QualityCategory.Physical)) 12 else 4
    assertEquals(result, expectedMax, "Should respect archetype extra health categories")

  test("StagingKey type includes all expected types"):
    // Test that StagingKey union type works correctly
    val backgroundKey: CharacterComputation.StagingKey = Background.created
    val powerSourceKey: CharacterComputation.StagingKey = Accident.accident
    val archetypeKey: CharacterComputation.StagingKey = Gadgeteer.gadgeteer
    val personalityKey: CharacterComputation.StagingKey = Personality.loneWolf
    val redAbilityKey: CharacterComputation.StagingKey = RedAbility.redAbilityPhase

    // All should compile and be valid StagingKeys
    assert(backgroundKey.isInstanceOf[Background], "Background should be a valid StagingKey")
    assert(powerSourceKey.isInstanceOf[PowerSource], "PowerSource should be a valid StagingKey")
    assert(archetypeKey.isInstanceOf[Archetype], "Archetype should be a valid StagingKey")
    assert(personalityKey.isInstanceOf[Personality], "Personality should be a valid StagingKey")
    assert(redAbilityKey.isInstanceOf[RedAbility.RedAbilityPhase], "RedAbilityPhase should be a valid StagingKey")

  test("computeAllQualities with complex multi-source data"):
    val bg = Background.created
    val ps = Accident.accident
    val at = Gadgeteer.gadgeteer
    val pt = Personality.loneWolf

    val qualityStaging: Map[CharacterComputation.StagingKey, List[(Quality, Die)]] = Map(
      bg -> List((Quality.alertness, Die.d(6)), (Quality.conviction, Die.d(8))),
      ps -> List((Quality.fitness, Die.d(10))),
      at -> List((Quality.technology, Die.d(12))),
      pt -> List((pt.baseQuality, Die.d(8)))
    )

    val result = CharacterComputation.computeAllQualities(
      qualityStaging = qualityStaging,
      background = Some(bg),
      powerSource = Some(ps),
      archetype = Some(at),
      personality = Some(pt),
      dieChanges = Map(),
      allDieChangesFunction = (_, _, _, _) => Map()
    )

    // Should include qualities from all sources
    assertEquals(result.length, 5, "Should have qualities from all 4 sources")
    assert(result.exists(_._1 == Quality.alertness), "Should include background quality")
    assert(result.exists(_._1 == Quality.fitness), "Should include power source quality")
    assert(result.exists(_._1 == Quality.technology), "Should include archetype quality")
    assert(result.exists(_._1 == pt.baseQuality), "Should include personality quality")

  test("computeAllQualities with die changes across multiple sources"):
    val bg = Background.created
    val ps = Accident.accident

    val qualityStaging: Map[CharacterComputation.StagingKey, List[(Quality, Die)]] = Map(
      bg -> List((Quality.alertness, Die.d(6))),
      ps -> List((Quality.fitness, Die.d(8)))
    )

    val dieChanges: Map[CharacterComputation.StagingKey, Map[Quality | Power, CharacterComputation.DieChange]] = Map(
      bg -> Map(Quality.alertness -> CharacterComputation.DieChange.Upgrade),
      ps -> Map(Quality.fitness -> CharacterComputation.DieChange.Downgrade)
    )

    val result = CharacterComputation.computeAllQualities(
      qualityStaging = qualityStaging,
      background = Some(bg),
      powerSource = Some(ps),
      archetype = None,
      personality = None,
      dieChanges = dieChanges,
      allDieChangesFunction = (dc, mps, mat, mpt) => {
        // Correctly combine die changes - background is handled separately by the computation function
        val bgChanges = dc.getOrElse(Background.created, Map())
        val psChanges = mps.fold(Map[Quality | Power, CharacterComputation.DieChange]())(ps => dc.getOrElse(ps, Map()))
        val atChanges = mat.fold(Map[Quality | Power, CharacterComputation.DieChange]())(at => dc.getOrElse(at, Map()))
        val ptChanges = mpt.fold(Map[Quality | Power, CharacterComputation.DieChange]())(pt => dc.getOrElse(pt, Map()))
        bgChanges ++ psChanges ++ atChanges ++ ptChanges
      }
    )

    assertEquals(result.length, 2, "Should have 2 qualities with die changes applied")
    val alertnessResult = result.find(_._1 == Quality.alertness).map(_._2)
    val fitnessResult = result.find(_._1 == Quality.fitness).map(_._2)


    assertEquals(alertnessResult, Some(Die.d(8)), "Alertness should be upgraded from d6 to d8")
    assertEquals(fitnessResult, Some(Die.d(6)), "Fitness should be downgraded from d8 to d6 (this IS correct)")

  test("computePowerQualityHealth with complex archetype and personality"):
    val at = Gadgeteer.gadgeteer
    val pt = Personality.loneWolf

    // Mix of different power and quality categories
    val allPowers = List(
      (Power.strength, Die.d(6)),  // Athletic
      (Power.fire, Die.d(8))       // Elemental
    )
    val allQualities = List(
      (Quality.fitness, Die.d(10)), // Physical
      (Quality.alertness, Die.d(12)), // Mental
      (Quality.technology, Die.d(6))  // Intellectual
    )

    val result = CharacterComputation.computePowerQualityHealth(
      allPowers = allPowers,
      allQualities = allQualities,
      archetype = Some(at),
      personality = Some(pt)
    )

    // Should calculate max from appropriate categories based on archetype rules
    // Need to check what categories Gadgeteer actually allows for health
    val maxDie = (allPowers.filter(_._1.category == PowerCategory.Athletic).map(_._2.n) ++
                  allQualities.filter(q => q._1.category == QualityCategory.Mental ||
                                           at.extraHealthCategories.contains(q._1.category)).map(_._2.n)).maxOption.getOrElse(4)
    assertEquals(result, maxDie, s"Health should be calculated from appropriate power/quality categories, expected $maxDie")

  test("computeAllPowers with power source and archetype"):
    val ps = Accident.accident
    val at = Gadgeteer.gadgeteer

    val powerStaging: Map[CharacterComputation.StagingKey, List[(Power, Die)]] = Map(
      ps -> List((Power.strength, Die.d(8)), (Power.fire, Die.d(6))),
      at -> List((Power.gadgets, Die.d(10)))
    )

    val result = CharacterComputation.computeAllPowers(
      powerStaging = powerStaging,
      background = None,
      powerSource = Some(ps),
      archetype = Some(at),
      personality = None,
      dieChanges = Map(),
      allDieChangesFunction = (_, _, _, _) => Map()
    )

    assertEquals(result.length, 3, "Should include powers from both power source and archetype")
    assert(result.exists(_._1 == Power.strength), "Should include power source power")
    assert(result.exists(_._1 == Power.gadgets), "Should include archetype power")

end CharacterComputationSpec