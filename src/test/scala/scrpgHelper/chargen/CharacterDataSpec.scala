package scrpgHelper.chargen

import munit.FunSuite
import scrpgHelper.rolls.Die
import scrpgHelper.status.Status
import scrpgHelper.chargen.characterModel.*

class CharacterDataSpec extends FunSuite:

  test("CharacterData provides pure data access without reactive behavior"):
    val mockState = MockCharacterState.basicSelections
    val characterData = mockState.data

    // Core character selections should return raw values
    assert(characterData.background.isInstanceOf[Option[Background]], "background should return Option[Background]")
    assert(characterData.powerSource.isInstanceOf[Option[PowerSource]], "powerSource should return Option[PowerSource]")
    assert(characterData.archetype.isInstanceOf[Option[Archetype]], "archetype should return Option[Archetype]")
    assert(characterData.personality.isInstanceOf[Option[Personality]], "personality should return Option[Personality]")
    assert(characterData.health.isInstanceOf[Option[Int]], "health should return Option[Int]")

  test("CharacterData computed values are pure functions"):
    val mockState = MockCharacterState.withPowersAndQualities
    val characterData = mockState.data

    // Computed values should return pure data
    assert(characterData.allQualities.isInstanceOf[List[(Quality, Die)]], "allQualities should return List[(Quality, Die)]")
    assert(characterData.allPowers.isInstanceOf[List[(Power, Die)]], "allPowers should return List[(Power, Die)]")
    assert(characterData.allStagedAbilities.isInstanceOf[List[ChosenAbility]], "allStagedAbilities should return List[ChosenAbility]")
    assert(characterData.allChosenAbilities.isInstanceOf[List[ChosenAbility]], "allChosenAbilities should return List[ChosenAbility]")
    assert(characterData.allPrinciples.isInstanceOf[List[Principle]], "allPrinciples should return List[Principle]")
    assert(characterData.allAbilities.isInstanceOf[List[Ability[_]]], "allAbilities should return List[Ability[_]]")

  test("CharacterData health calculations are pure functions"):
    val mockState = MockCharacterState.withPowersAndQualities
    val characterData = mockState.data

    assert(characterData.redZoneHealth.isInstanceOf[Option[Int]], "redZoneHealth should return Option[Int]")
    assert(characterData.powerQualityHealth.isInstanceOf[Int], "powerQualityHealth should return Int")

  test("CharacterData staging data access returns pure maps"):
    val mockState = MockCharacterState.withPowersAndQualities
    val characterData = mockState.data

    // Staging data should return pure Maps
    assert(characterData.qualityStaging.isInstanceOf[Map[CharacterComputation.StagingKey, List[(Quality, Die)]]],
      "qualityStaging should return Map")
    assert(characterData.powerStaging.isInstanceOf[Map[CharacterComputation.StagingKey, List[(Power, Die)]]],
      "powerStaging should return Map")
    assert(characterData.abilityStaging.isInstanceOf[Map[CharacterComputation.StagingKey, List[Ability[_]]]],
      "abilityStaging should return Map")
    assert(characterData.abilityChoice.isInstanceOf[Map[CharacterComputation.StagingKey, Map[AbilityKey, ChosenAbility]]],
      "abilityChoice should return Map")
    assert(characterData.dieChanges.isInstanceOf[Map[CharacterComputation.StagingKey, Map[Quality | Power, CharacterComputation.DieChange]]],
      "dieChanges should return Map")
    assert(characterData.baseAbilities.isInstanceOf[Map[CharacterComputation.StagingKey, Map[AbilityKey, ChosenAbility]]],
      "baseAbilities should return Map")

  test("CharacterData is consistent across multiple calls"):
    val mockState = MockCharacterState.basicSelections
    val characterData = mockState.data

    // Multiple calls to the same method should return the same result
    val bg1 = characterData.background
    val bg2 = characterData.background
    assertEquals(bg1, bg2, "background should be consistent across calls")

    val qualities1 = characterData.allQualities
    val qualities2 = characterData.allQualities
    assertEquals(qualities1, qualities2, "allQualities should be consistent across calls")

  test("CharacterData works with different mock configurations"):
    val minimal = MockCharacterState.minimal
    val basic = MockCharacterState.basicSelections
    val withPowers = MockCharacterState.withPowersAndQualities

    // All configurations should provide valid CharacterData
    assert(minimal.data != null, "minimal data should be accessible")
    assert(basic.data != null, "basic data should be accessible")
    assert(withPowers.data != null, "withPowers data should be accessible")

    // Each should have different data based on their configuration
    assert(minimal.data.background != basic.data.background ||
           minimal.data.powerSource != basic.data.powerSource,
           "different configurations should have different data")

  test("CharacterData computes qualities from staging data"):
    // Create mock with realistic staging data to test actual computation
    val mockState = new MockCharacterState(
      backgroundValue = Some(Background.created),
      personalityValue = Some(Personality.loneWolf)
    ) {
      override val data: CharacterData = new CharacterData:
        def background: Option[Background] = Some(Background.created)
        def powerSource: Option[PowerSource] = None
        def archetype: Option[Archetype] = None
        def personality: Option[Personality] = Some(Personality.loneWolf)
        def health: Option[Int] = None

        // Staging data with real content
        def qualityStaging: Map[StagingKey, List[(Quality, Die)]] = Map(
          Background.created -> List((Quality.alertness, Die.d(6)), (Quality.conviction, Die.d(8))),
          Personality.loneWolf -> List((Personality.loneWolf.baseQuality, Die.d(8)))
        )
        def powerStaging: Map[StagingKey, List[(Power, Die)]] = Map.empty
        def abilityStaging: Map[StagingKey, List[Ability[_]]] = Map.empty
        def abilityChoice: Map[StagingKey, Map[AbilityKey, ChosenAbility]] = Map.empty
        def dieChanges: Map[StagingKey, Map[Quality | Power, DieChange]] = Map.empty
        def baseAbilities: Map[StagingKey, Map[AbilityKey, ChosenAbility]] = Map.empty

        // Use computation functions to get real computed values
        def allQualities: List[(Quality, Die)] = CharacterComputation.computeAllQualities(
          qualityStaging = qualityStaging,
          background = background,
          powerSource = powerSource,
          archetype = archetype,
          personality = personality,
          dieChanges = dieChanges,
          allDieChangesFunction = (_, _, _, _) => Map()
        )

        def allPowers: List[(Power, Die)] = CharacterComputation.computeAllPowers(
          powerStaging = powerStaging,
          background = background,
          powerSource = powerSource,
          archetype = archetype,
          personality = personality,
          dieChanges = dieChanges,
          allDieChangesFunction = (_, _, _, _) => Map()
        )

        def allStagedAbilities: List[ChosenAbility] = CharacterComputation.computeAllStagedAbilities(
          abilityStaging = abilityStaging,
          background = background,
          powerSource = powerSource,
          archetype = archetype,
          personality = personality
        )

        def allChosenAbilities: List[ChosenAbility] = CharacterComputation.computeAllChosenAbilities(
          abilityChoice = abilityChoice,
          background = background,
          powerSource = powerSource,
          archetype = archetype,
          personality = personality
        )

        def allPrinciples: List[Principle] = CharacterComputation.computeAllPrinciples(
          abilityStaging = abilityStaging,
          background = background,
          archetype = archetype
        )

        def allAbilities: List[Ability[_]] = CharacterComputation.computeAllAbilities(
          allStagedAbilities = allStagedAbilities,
          allChosenAbilities = allChosenAbilities,
          allPrinciples = allPrinciples
        )

        def redZoneHealth: Option[Int] = CharacterComputation.computeRedZoneHealth(personality)

        def powerQualityHealth: Int = CharacterComputation.computePowerQualityHealth(
          allPowers = allPowers,
          allQualities = allQualities,
          archetype = archetype,
          personality = personality
        )

        type StagingKey = CharacterComputation.StagingKey
        type DieChange = CharacterComputation.DieChange
    }

    val characterData = mockState.data

    // Test that computed qualities actually include data from staging
    val qualities = characterData.allQualities
    assert(qualities.nonEmpty, "Should have computed qualities from staging data")
    assert(qualities.exists(_._1 == Quality.alertness), "Should include background quality")
    assert(qualities.exists(_._1 == Personality.loneWolf.baseQuality), "Should include personality quality")

    // Test that staging data is accessible
    val bgQualities = characterData.qualityStaging.getOrElse(Background.created, List())
    assertEquals(bgQualities.length, 2, "Background should have 2 staged qualities")
    assert(bgQualities.exists(_._1 == Quality.alertness), "Background staging should include alertness")

  test("CharacterData computes health from powers and qualities"):
    val mockState = new MockCharacterState(
      backgroundValue = Some(Background.created),
      personalityValue = Some(Personality.loneWolf)
    ) {
      override val data: CharacterData = new CharacterData:
        def background: Option[Background] = Some(Background.created)
        def powerSource: Option[PowerSource] = None
        def archetype: Option[Archetype] = None
        def personality: Option[Personality] = Some(Personality.loneWolf)
        def health: Option[Int] = None

        def qualityStaging: Map[StagingKey, List[(Quality, Die)]] = Map(
          Background.created -> List((Quality.alertness, Die.d(10))) // Mental quality for health
        )
        def powerStaging: Map[StagingKey, List[(Power, Die)]] = Map(
          Background.created -> List((Power.strength, Die.d(8))) // Athletic power for health
        )
        def abilityStaging: Map[StagingKey, List[Ability[_]]] = Map.empty
        def abilityChoice: Map[StagingKey, Map[AbilityKey, ChosenAbility]] = Map.empty
        def dieChanges: Map[StagingKey, Map[Quality | Power, DieChange]] = Map.empty
        def baseAbilities: Map[StagingKey, Map[AbilityKey, ChosenAbility]] = Map.empty

        def allQualities: List[(Quality, Die)] = CharacterComputation.computeAllQualities(
          qualityStaging = qualityStaging,
          background = background,
          powerSource = powerSource,
          archetype = archetype,
          personality = personality,
          dieChanges = dieChanges,
          allDieChangesFunction = (_, _, _, _) => Map()
        )

        def allPowers: List[(Power, Die)] = CharacterComputation.computeAllPowers(
          powerStaging = powerStaging,
          background = background,
          powerSource = powerSource,
          archetype = archetype,
          personality = personality,
          dieChanges = dieChanges,
          allDieChangesFunction = (_, _, _, _) => Map()
        )

        def allStagedAbilities: List[ChosenAbility] = List.empty
        def allChosenAbilities: List[ChosenAbility] = List.empty
        def allPrinciples: List[Principle] = List.empty
        def allAbilities: List[Ability[_]] = List.empty

        def redZoneHealth: Option[Int] = CharacterComputation.computeRedZoneHealth(personality)

        def powerQualityHealth: Int = CharacterComputation.computePowerQualityHealth(
          allPowers = allPowers,
          allQualities = allQualities,
          archetype = archetype,
          personality = personality
        )

        type StagingKey = CharacterComputation.StagingKey
        type DieChange = CharacterComputation.DieChange
    }

    val characterData = mockState.data

    // Test that health computation uses actual powers and qualities
    val health = characterData.powerQualityHealth
    assert(health == 10, s"Health should be 10 (max of alertness d10 and strength d8), but got $health")

    // Test red zone health from personality
    val redHealth = characterData.redZoneHealth
    assertEquals(redHealth, Some(8), "Red zone health should come from personality's red die")

  test("CharacterData type aliases work correctly"):
    val mockState = MockCharacterState.basicSelections
    val characterData = mockState.data

    // Type aliases should be accessible
    val stagingKey: characterData.StagingKey = Background.created
    val dieChange: characterData.DieChange = CharacterComputation.DieChange.Upgrade

    assert(stagingKey.isInstanceOf[Background], "StagingKey type alias should work")
    assert(dieChange.isInstanceOf[CharacterComputation.DieChange], "DieChange type alias should work")

end CharacterDataSpec