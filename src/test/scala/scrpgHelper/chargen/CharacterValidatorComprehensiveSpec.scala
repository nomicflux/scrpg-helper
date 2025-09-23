package scrpgHelper.chargen

import munit.FunSuite
import scrpgHelper.chargen.characterModel.*
import scrpgHelper.rolls.Die
import scrpgHelper.status.Status

class CharacterValidatorComprehensiveSpec extends FunSuite:
  import Die.d

  // Helper to fill out ability choices with valid values
  def fillAbilityChoices(baseChoices: List[AbilityChoice], power: Power, quality: Quality, energy: Energy): List[AbilityChoice] =
    baseChoices.map {
      case pqc: PowerQualityChoice => pqc.withChoice(power)
      case pc: PowerChoice => pc.withChoice(power)
      case qc: QualityChoice => qc.withChoice(quality)
      case ec: EnergyChoice => ec.withChoice(energy)
      case other => other
    }

  // Helper to create test character data with staging information
  def createTestCharacterData(
      bg: Option[Background] = None,
      ps: Option[PowerSource] = None,
      at: Option[Archetype] = None,
      pt: Option[Personality] = None,
      h: Option[Int] = None,
      qualityStagingParam: Map[CharacterComputation.StagingKey, List[(Quality, Die)]] = Map(),
      powerStagingParam: Map[CharacterComputation.StagingKey, List[(Power, Die)]] = Map(),
      abilityStagingParam: Map[CharacterComputation.StagingKey, List[Ability[_]]] = Map(),
      abilityChoiceParam: Map[CharacterComputation.StagingKey, Map[AbilityKey, ChosenAbility]] = Map()
  ): CharacterData = new CharacterData {
    def background = bg
    def powerSource = ps
    def archetype = at
    def personality = pt
    def health = h
    def allQualities = qualityStagingParam.values.flatten.toList
    def allPowers = powerStagingParam.values.flatten.toList
    def allStagedAbilities = abilityStagingParam.values.flatten.collect { case ca: ChosenAbility => ca }.toList
    def allChosenAbilities = List()
    def allPrinciples = abilityStagingParam.values.flatten.collect { case p: Principle => p }.toList
    def allAbilities = abilityStagingParam.values.flatten.toList
    def redZoneHealth = pt.flatMap(_.statusDice.get(Status.Red)).map(_.n)
    def powerQualityHealth = 4
    def qualityStaging = qualityStagingParam
    def powerStaging = powerStagingParam
    def abilityStaging = abilityStagingParam
    def abilityChoice = abilityChoiceParam
    def dieChanges = Map()
    def baseAbilities = Map()
  }

  // Sample principle for testing
  val samplePrinciple = Principle(
    new AbilityId,
    "Test Principle",
    AbilityCategory.Action,
    Set(),
    PrincipleCategory.Responsibility
  )

  // BACKGROUND VALIDATION TESTS
  test("Background validation - Upper Class with valid qualities and principle") {
    val bg = Background.upperClass
    val validQualities = List((Quality.fitness, d(10)), (Quality.alertness, d(8)))
    val validAbilities = List(samplePrinciple)

    val data = createTestCharacterData(
      bg = Some(bg),
      qualityStagingParam = Map(bg -> validQualities),
      abilityStagingParam = Map(bg -> validAbilities)
    )

    val validator = CharacterValidator(data)
    assert(validator.validBackground(data), "Upper Class should be valid with correct qualities and principle")
  }

  test("Background validation - Medical with mandatory medicine quality") {
    val bg = Background.medical
    val validQualities = List((Quality.medicine, d(10)), (Quality.finesse, d(8)), (Quality.science, d(6)))
    val validAbilities = List(samplePrinciple.copy(principleCategory = PrincipleCategory.Expertise))

    val data = createTestCharacterData(
      bg = Some(bg),
      qualityStagingParam = Map(bg -> validQualities),
      abilityStagingParam = Map(bg -> validAbilities)
    )

    val validator = CharacterValidator(data)
    assert(validator.validBackground(data), "Medical should be valid with medicine quality")
  }

  test("Background validation - Academic with correct number of dice") {
    val bg = Background.academic
    val validQualities = List((Quality.leadership, d(12)), (Quality.history, d(8)))
    val validAbilities = List(samplePrinciple.copy(principleCategory = PrincipleCategory.Expertise))

    val data = createTestCharacterData(
      bg = Some(bg),
      qualityStagingParam = Map(bg -> validQualities),
      abilityStagingParam = Map(bg -> validAbilities)
    )

    val validator = CharacterValidator(data)
    assert(validator.validBackground(data), "Academic should be valid with 2 dice matching backgroundDice")
  }

  test("Background validation - Struggling with correct categories") {
    val bg = Background.struggling
    val validQualities = List((Quality.banter, d(8)), (Quality.fitness, d(6)), (Quality.acrobatics, d(6)))
    val validAbilities = List(samplePrinciple)

    val data = createTestCharacterData(
      bg = Some(bg),
      qualityStagingParam = Map(bg -> validQualities),
      abilityStagingParam = Map(bg -> validAbilities)
    )

    val validator = CharacterValidator(data)
    assert(validator.validBackground(data), "Struggling should be valid with physical qualities")
  }

  test("Background validation - Tragic with wrong principle category fails") {
    val bg = Background.tragic
    val validQualities = List((Quality.banter, d(10)), (Quality.closeCombat, d(8)))
    val wrongPrinciple = List(samplePrinciple.copy(principleCategory = PrincipleCategory.Expertise)) // Should be Ideals

    val data = createTestCharacterData(
      bg = Some(bg),
      qualityStagingParam = Map(bg -> validQualities),
      abilityStagingParam = Map(bg -> wrongPrinciple)
    )

    val validator = CharacterValidator(data)
    assert(!validator.validBackground(data), "Tragic should fail with wrong principle category")
  }

  // PERSONALITY VALIDATION TESTS
  test("Personality validation - Lone Wolf with valid quality") {
    val pt = Personality.loneWolf
    val validQualities = List(Quality.personalityQuality("Custom Quality"))
    val validAbilities = List(pt.ability)

    val data = createTestCharacterData(
      pt = Some(pt),
      qualityStagingParam = Map(pt -> validQualities.map(q => (q, d(8)))),
      abilityChoiceParam = Map(pt -> Map(pt.ability.key -> pt.ability))
    )

    val validator = CharacterValidator(data)
    assert(validator.validPersonality(data), "Lone Wolf should be valid with named quality")
  }

  test("Personality validation - Natural Leader with proper setup") {
    val pt = Personality.naturalLeader
    val validQualities = List(Quality.personalityQuality("Leadership Quality"))
    val validAbilities = List(pt.ability)

    val data = createTestCharacterData(
      pt = Some(pt),
      qualityStagingParam = Map(pt -> validQualities.map(q => (q, d(8)))),
      abilityChoiceParam = Map(pt -> Map(pt.ability.key -> pt.ability))
    )

    val validator = CharacterValidator(data)
    assert(validator.validPersonality(data), "Natural Leader should be valid")
  }

  test("Personality validation - Impulsive with upgrade capability") {
    val pt = Personality.impulsive
    val validQualities = List(Quality.personalityQuality("Impulse Control"))

    val data = createTestCharacterData(
      pt = Some(pt),
      qualityStagingParam = Map(pt -> validQualities.map(q => (q, d(8))))
    )

    val validator = CharacterValidator(data)
    assert(validator.validPersonality(data), "Impulsive should be valid")
  }

  test("Personality validation - Mischievous with extra health check") {
    val pt = Personality.mischievous
    val validQualities = List(Quality.personalityQuality("Mischief"))

    val data = createTestCharacterData(
      pt = Some(pt),
      qualityStagingParam = Map(pt -> validQualities.map(q => (q, d(8))))
    )

    val validator = CharacterValidator(data)
    assert(validator.validPersonality(data), "Mischievous should be valid")
  }

  test("Personality validation - Sarcastic with typical setup") {
    val pt = Personality.sarcastic
    val validQualities = List(Quality.personalityQuality("Wit"))

    val data = createTestCharacterData(
      pt = Some(pt),
      qualityStagingParam = Map(pt -> validQualities.map(q => (q, d(8))))
    )

    val validator = CharacterValidator(data)
    assert(validator.validPersonality(data), "Sarcastic should be valid")
  }

  test("Personality validation fails with empty quality name") {
    val pt = Personality.loneWolf
    val invalidQualities = List(Quality.personalityQuality("")) // Empty name

    val data = createTestCharacterData(
      pt = Some(pt),
      qualityStagingParam = Map(pt -> invalidQualities.map(q => (q, d(8))))
    )

    val validator = CharacterValidator(data)
    assert(!validator.validPersonality(data), "Should fail with empty quality name")
  }

  // POWER SOURCE VALIDATION TESTS
  test("PowerSource validation - Training with correct abilities and powers") {
    val ps = PowerSource.powerSources.find(_.name == "Training").get
    val bg = Background.upperClass // Provides 3 power source dice: d10, d8, d8
    val validPowers = List((Power.signatureWeapon, d(10)), (Power.gadgets, d(8)), (Power.agility, d(8)))
    val validQualities = List() // Training doesn't require extra qualities
    val sampleAbility1 = ps.abilityPools.head.abilities.head.toChosenAbility(ps.abilityPools.head)
    val sampleAbility2 = ps.abilityPools.head.abilities(1).toChosenAbility(ps.abilityPools.head)

    // Fill the PowerChoice in "Always Be Prepared" ability
    val filledAbility1 = sampleAbility1.copy(currentChoices = sampleAbility1.currentChoices.map {
      case pc: PowerChoice => pc.withChoice(Power.gadgets)
      case other => other
    })
    val filledAbility2 = sampleAbility2 // "Reactive Field" doesn't need choices

    val data = createTestCharacterData(
      bg = Some(bg),
      ps = Some(ps),
      powerStagingParam = Map(ps -> validPowers),
      qualityStagingParam = Map(ps -> validQualities.map(q => (q, d(8)))),
      abilityStagingParam = Map(ps -> List(filledAbility1, filledAbility2)),
      abilityChoiceParam = Map(ps -> Map(
        filledAbility1.key -> filledAbility1,
        filledAbility2.key -> filledAbility2
      ))
    )

    val validator = CharacterValidator(data)
    assert(validator.validPowerSource(data), "Training should be valid with correct setup")
  }

  test("PowerSource validation - Genetic with signature powers") {
    val ps = PowerSource.powerSources.find(_.name == "Genetic").get
    val bg = Background.upperClass
    val validPowers = List((Power.agility, d(10)), (Power.strength, d(8)), (Power.vitality, d(8)))
    val sampleAbility1 = ps.abilityPools(0).abilities.head.toChosenAbility(ps.abilityPools(0))
    val sampleAbility2 = ps.abilityPools(0).abilities(1).toChosenAbility(ps.abilityPools(0))
    val sampleAbility3 = ps.abilityPools(1).abilities.head.toChosenAbility(ps.abilityPools(1))

    // Fill PowerChoice in first two abilities, QualityChoice in third ability
    val filledAbility1 = sampleAbility1.copy(currentChoices = sampleAbility1.currentChoices.map {
      case pc: PowerChoice => pc.withChoice(Power.agility)
      case other => other
    })
    val filledAbility2 = sampleAbility2.copy(currentChoices = sampleAbility2.currentChoices.map {
      case pc: PowerChoice => pc.withChoice(Power.strength)
      case other => other
    })
    val filledAbility3 = sampleAbility3.copy(currentChoices = sampleAbility3.currentChoices.map {
      case qc: QualityChoice => qc.withChoice(Quality.alertness)
      case other => other
    })

    val data = createTestCharacterData(
      bg = Some(bg),
      ps = Some(ps),
      powerStagingParam = Map(ps -> validPowers),
      abilityStagingParam = Map(ps -> List(filledAbility1, filledAbility2, filledAbility3)),
      abilityChoiceParam = Map(ps -> Map(
        filledAbility1.key -> filledAbility1,
        filledAbility2.key -> filledAbility2,
        filledAbility3.key -> filledAbility3
      ))
    )

    val validator = CharacterValidator(data)
    assert(validator.validPowerSource(data), "Genetic should be valid with abilities")
  }

  test("PowerSource validation - Accident with basic setup") {
    val ps = PowerSource.powerSources.find(_.name == "Accident").get
    val bg = Background.upperClass
    val validPowers = List((Power.flight, d(10)), (Power.vitality, d(8)), (Power.strength, d(8)))
    val sampleAbility1 = ps.abilityPools(0).abilities.head.toChosenAbility(ps.abilityPools(0))
    val sampleAbility2 = ps.abilityPools(0).abilities(1).toChosenAbility(ps.abilityPools(0))
    val sampleAbility3 = ps.abilityPools(1).abilities.head.toChosenAbility(ps.abilityPools(1))

    // Fill PowerChoice in abilities that need them
    val filledAbility1 = sampleAbility1.copy(currentChoices = sampleAbility1.currentChoices.map {
      case pc: PowerChoice => pc.withChoice(Power.flight)
      case ac: ActionChoice => ac.withChoice(Action.Boost)
      case other => other
    })
    val filledAbility2 = sampleAbility2.copy(currentChoices = sampleAbility2.currentChoices.map {
      case pc: PowerChoice => pc.withChoice(Power.vitality)
      case other => other
    })
    val filledAbility3 = sampleAbility3.copy(currentChoices = sampleAbility3.currentChoices.map {
      case pc: PowerChoice => pc.withChoice(Power.strength)
      case other => other
    })

    val data = createTestCharacterData(
      bg = Some(bg),
      ps = Some(ps),
      powerStagingParam = Map(ps -> validPowers),
      abilityStagingParam = Map(ps -> List(filledAbility1, filledAbility2, filledAbility3)),
      abilityChoiceParam = Map(ps -> Map(
        filledAbility1.key -> filledAbility1,
        filledAbility2.key -> filledAbility2,
        filledAbility3.key -> filledAbility3
      ))
    )

    val validator = CharacterValidator(data)
    assert(validator.validPowerSource(data), "Accident should be valid with basic setup")
  }

  test("PowerSource validation - Mystical with special requirements") {
    val ps = PowerSource.powerSources.find(_.name == "Mystical").get
    val bg = Background.upperClass
    val validPowers = List((Power.precognition, d(10)), (Power.vitality, d(8)), (Power.flight, d(8)))
    val validQualities = List(Quality.investigation) // Information quality required
    val sampleAbility1 = ps.abilityPools.head.abilities.head.toChosenAbility(ps.abilityPools.head)
    val sampleAbility2 = ps.abilityPools.head.abilities(1).toChosenAbility(ps.abilityPools.head)

    // Fill PowerChoice in abilities
    val filledAbility1 = sampleAbility1.copy(currentChoices = sampleAbility1.currentChoices.map {
      case pc: PowerChoice => pc.withChoice(Power.precognition)
      case other => other
    })
    val filledAbility2 = sampleAbility2.copy(currentChoices = sampleAbility2.currentChoices.map {
      case pc: PowerChoice => pc.withChoice(Power.vitality)
      case other => other
    })

    val data = createTestCharacterData(
      bg = Some(bg),
      ps = Some(ps),
      powerStagingParam = Map(ps -> validPowers),
      qualityStagingParam = Map(ps -> validQualities.map(q => (q, d(10)))),
      abilityStagingParam = Map(ps -> List(filledAbility1, filledAbility2)),
      abilityChoiceParam = Map(ps -> Map(
        filledAbility1.key -> filledAbility1,
        filledAbility2.key -> filledAbility2
      ))
    )

    val validator = CharacterValidator(data)
    assert(validator.validPowerSource(data), "Mystical should be valid")
  }

  test("PowerSource validation - Experimentation with complex setup") {
    val ps = PowerSource.powerSources.find(_.name == "Experimentation").get
    val bg = Background.upperClass
    val validPowers = List((Power.vitality, d(10)), (Power.absorption, d(8)), (Power.signatureWeapon, d(8)))
    val sampleAbility1 = ps.abilityPools(0).abilities.head.toChosenAbility(ps.abilityPools(0))
    val sampleAbility2 = ps.abilityPools(0).abilities(1).toChosenAbility(ps.abilityPools(0))
    val sampleAbility3 = ps.abilityPools(1).abilities.head.toChosenAbility(ps.abilityPools(1))

    // Fill PowerChoice in abilities that need them
    val filledAbility1 = sampleAbility1.copy(currentChoices = sampleAbility1.currentChoices.map {
      case pc: PowerChoice => pc.withChoice(Power.vitality)
      case other => other
    })
    val filledAbility2 = sampleAbility2.copy(currentChoices = sampleAbility2.currentChoices.map {
      case pc: PowerChoice => pc.withChoice(Power.absorption)
      case other => other
    })
    val filledAbility3 = sampleAbility3 // Inherent abilities don't need choices

    val data = createTestCharacterData(
      bg = Some(bg),
      ps = Some(ps),
      powerStagingParam = Map(ps -> validPowers),
      abilityStagingParam = Map(ps -> List(filledAbility1, filledAbility2, filledAbility3)),
      abilityChoiceParam = Map(ps -> Map(
        filledAbility1.key -> filledAbility1,
        filledAbility2.key -> filledAbility2,
        filledAbility3.key -> filledAbility3
      ))
    )

    val validator = CharacterValidator(data)
    assert(validator.validPowerSource(data), "Experimentation should be valid")
  }

  // ARCHETYPE VALIDATION TESTS
  test("Archetype validation - Speedster with signature power") {
    val at = Archetype.archetypes.find(_.name == "Speedster").get
    val ps = PowerSource.powerSources.find(_.name == "Training").get

    // Need enough powers and qualities to match dice pool (3 dice total)
    val validPowers = List(Power.speed, Power.agility) // 2 powers
    val validQualities = List(Quality.alertness) // 1 quality = 3 total items

    // Create abilities with filled out choices
    val ability1Template = at.abilityPools(0).abilities.head
    val ability2Template = at.abilityPools(0).abilities(1)
    val ability3Template = at.abilityPools(1).abilities.head

    // Fill out the choices with actual values
    val filledChoices1 = ability1Template.baseChoices.map {
      case pqc: PowerQualityChoice => pqc.withChoice(Power.speed)
      case pc: PowerChoice => pc.withChoice(Power.speed)
      case qc: QualityChoice => qc.withChoice(Quality.alertness)
      case other => other
    }
    val filledChoices2 = ability2Template.baseChoices.map {
      case pqc: PowerQualityChoice => pqc.withChoice(Power.agility)
      case pc: PowerChoice => pc.withChoice(Power.agility)
      case qc: QualityChoice => qc.withChoice(Quality.alertness)
      case other => other
    }
    val filledChoices3 = ability3Template.baseChoices.map {
      case pqc: PowerQualityChoice => pqc.withChoice(Power.speed)
      case pc: PowerChoice => pc.withChoice(Power.speed)
      case qc: QualityChoice => qc.withChoice(Quality.alertness)
      case other => other
    }

    val sampleAbility1 = ability1Template.toChosenAbility(at.abilityPools(0)).copy(currentChoices = filledChoices1)
    val sampleAbility2 = ability2Template.toChosenAbility(at.abilityPools(0)).copy(currentChoices = filledChoices2)
    val sampleAbility3 = ability3Template.toChosenAbility(at.abilityPools(1)).copy(currentChoices = filledChoices3)

    val data = createTestCharacterData(
      ps = Some(ps),
      at = Some(at),
      powerStagingParam = Map(
        ps -> List((Power.speed, d(10)), (Power.agility, d(8))),
        at -> validPowers.map(p => (p, d(8)))
      ),
      qualityStagingParam = Map(at -> validQualities.map(q => (q, d(8)))),
      abilityStagingParam = Map(at -> List(sampleAbility1, sampleAbility2, sampleAbility3)),
      abilityChoiceParam = Map(at -> Map(
        sampleAbility1.key -> sampleAbility1,
        sampleAbility2.key -> sampleAbility2,
        sampleAbility3.key -> sampleAbility3
      ))
    )

    val validator = CharacterValidator(data)
    assert(validator.validArchetype(data), "Speedster should be valid with speed power")
  }

  test("Archetype validation - Blaster with energy power category") {
    val at = Archetype.archetypes.find(_.name == "Blaster").get
    val ps = PowerSource.powerSources.find(_.name == "Training").get

    val energyPower = Power.fire // Energy category power
    // Need enough powers and qualities to match dice pool (3 dice total)
    val validPowers = List(energyPower, Power.signatureWeapon) // 2 powers
    val validQualities = List(Quality.alertness) // 1 quality = 3 total items

    // Create abilities with filled out choices
    val ability1Template = at.abilityPools(0).abilities.head
    val ability2Template = at.abilityPools(0).abilities(1)
    val ability3Template = at.abilityPools(1).abilities.head
    val ability4Template = at.abilityPools(1).abilities(1)

    // Fill out the choices with actual values
    val filledChoices1 = ability1Template.baseChoices.map {
      case pqc: PowerQualityChoice => pqc.withChoice(energyPower)
      case pc: PowerChoice => pc.withChoice(energyPower)
      case qc: QualityChoice => qc.withChoice(Quality.alertness)
      case ec: EnergyChoice => ec.withChoice(Energy.Fire)
      case other => other
    }
    val filledChoices2 = ability2Template.baseChoices.map {
      case pqc: PowerQualityChoice => pqc.withChoice(energyPower)
      case pc: PowerChoice => pc.withChoice(energyPower)
      case qc: QualityChoice => qc.withChoice(Quality.alertness)
      case ec: EnergyChoice => ec.withChoice(Energy.Fire)
      case other => other
    }
    val filledChoices3 = ability3Template.baseChoices.map {
      case pqc: PowerQualityChoice => pqc.withChoice(energyPower)
      case pc: PowerChoice => pc.withChoice(energyPower)
      case qc: QualityChoice => qc.withChoice(Quality.alertness)
      case ec: EnergyChoice => ec.withChoice(Energy.Fire)
      case other => other
    }
    val filledChoices4 = ability4Template.baseChoices.map {
      case pqc: PowerQualityChoice => pqc.withChoice(energyPower)
      case pc: PowerChoice => pc.withChoice(energyPower)
      case qc: QualityChoice => qc.withChoice(Quality.alertness)
      case ec: EnergyChoice => ec.withChoice(Energy.Fire)
      case other => other
    }

    val sampleAbility1 = ability1Template.toChosenAbility(at.abilityPools(0)).copy(currentChoices = filledChoices1)
    val sampleAbility2 = ability2Template.toChosenAbility(at.abilityPools(0)).copy(currentChoices = filledChoices2)
    val sampleAbility3 = ability3Template.toChosenAbility(at.abilityPools(1)).copy(currentChoices = filledChoices3)
    val sampleAbility4 = ability4Template.toChosenAbility(at.abilityPools(1)).copy(currentChoices = filledChoices4)

    val data = createTestCharacterData(
      ps = Some(ps),
      at = Some(at),
      powerStagingParam = Map(
        ps -> List((energyPower, d(10)), (Power.gadgets, d(8))),
        at -> validPowers.map(p => (p, d(8)))
      ),
      qualityStagingParam = Map(at -> validQualities.map(q => (q, d(8)))),
      abilityStagingParam = Map(at -> List(sampleAbility1, sampleAbility2, sampleAbility3, sampleAbility4)),
      abilityChoiceParam = Map(at -> Map(
        sampleAbility1.key -> sampleAbility1,
        sampleAbility2.key -> sampleAbility2,
        sampleAbility3.key -> sampleAbility3,
        sampleAbility4.key -> sampleAbility4
      ))
    )

    val validator = CharacterValidator(data)
    assert(validator.validArchetype(data), "Blaster should be valid with energy power")
  }

  test("Archetype validation - Shadow with stealth requirements") {
    val at = Archetype.archetypes.find(_.name == "Shadow").get
    val ps = PowerSource.powerSources.find(_.name == "Training").get

    // Need enough powers and qualities to match dice pool (3 dice total)
    val validPowers = List(Power.invisibility, Power.gadgets) // 2 powers
    val validQualities = List(Quality.stealth) // 1 quality = 3 total items (Shadow needs Stealth!)

    // Create abilities with filled out choices for ALL ability pools
    val allAbilities = at.abilityPools.zipWithIndex.flatMap { case (pool, poolIndex) =>
      pool.abilities.take(pool.max).map { template =>
        template.toChosenAbility(pool)
          .copy(currentChoices = fillAbilityChoices(template.baseChoices, Power.invisibility, Quality.stealth, Energy.Fire))
      }
    }

    val data = createTestCharacterData(
      ps = Some(ps),
      at = Some(at),
      powerStagingParam = Map(
        ps -> List((Power.invisibility, d(10)), (Power.gadgets, d(8))),
        at -> validPowers.map(p => (p, d(8)))
      ),
      qualityStagingParam = Map(at -> validQualities.map(q => (q, d(8)))),
      abilityStagingParam = Map(at -> allAbilities),
      abilityChoiceParam = Map(at -> allAbilities.map(a => a.key -> a).toMap)
    )

    val validator = CharacterValidator(data)
    assert(validator.validArchetype(data), "Shadow should be valid with invisibility")
  }

  test("Archetype validation - Physical Powerhouse with strength") {
    val at = Archetype.archetypes.find(_.name == "Physical Powerhouse").get
    val ps = PowerSource.powerSources.find(_.name == "Genetic").get

    // Need enough powers and qualities to match dice pool (3 dice total)
    val validPowers = List(Power.strength, Power.vitality) // 2 powers
    val validQualities = List(Quality.fitness) // 1 quality = 3 total items

    // Create abilities with filled out choices for ALL ability pools
    val allAbilities = at.abilityPools.zipWithIndex.flatMap { case (pool, poolIndex) =>
      pool.abilities.take(pool.max).map { template =>
        template.toChosenAbility(pool)
          .copy(currentChoices = fillAbilityChoices(template.baseChoices, Power.strength, Quality.fitness, Energy.Fire))
      }
    }

    val data = createTestCharacterData(
      ps = Some(ps),
      at = Some(at),
      powerStagingParam = Map(
        ps -> List((Power.strength, d(10)), (Power.vitality, d(8))),
        at -> validPowers.map(p => (p, d(8)))
      ),
      qualityStagingParam = Map(at -> validQualities.map(q => (q, d(8)))),
      abilityStagingParam = Map(at -> allAbilities),
      abilityChoiceParam = Map(at -> allAbilities.map(a => a.key -> a).toMap)
    )

    val validator = CharacterValidator(data)
    assert(validator.validArchetype(data), "Physical Powerhouse should be valid with strength")
  }

  test("Archetype validation - Marksman with ranged focus") {
    val at = Archetype.archetypes.find(_.name == "Marksman").get
    val ps = PowerSource.powerSources.find(_.name == "Training").get

    // Need enough powers and qualities to match dice pool (3 dice total)
    val validPowers = List(Power.signatureWeapon, Power.gadgets) // 2 powers
    val validQualities = List(Quality.alertness) // 1 quality = 3 total items

    // Create abilities with filled out choices for ALL ability pools
    val allAbilities = at.abilityPools.zipWithIndex.flatMap { case (pool, poolIndex) =>
      pool.abilities.take(pool.max).map { template =>
        template.toChosenAbility(pool)
          .copy(currentChoices = fillAbilityChoices(template.baseChoices, Power.signatureWeapon, Quality.alertness, Energy.Fire))
      }
    }

    val data = createTestCharacterData(
      ps = Some(ps),
      at = Some(at),
      powerStagingParam = Map(
        ps -> List((Power.signatureWeapon, d(10)), (Power.gadgets, d(8))),
        at -> validPowers.map(p => (p, d(8)))
      ),
      qualityStagingParam = Map(at -> validQualities.map(q => (q, d(8)))),
      abilityStagingParam = Map(at -> allAbilities),
      abilityChoiceParam = Map(at -> allAbilities.map(a => a.key -> a).toMap)
    )

    val validator = CharacterValidator(data)
    assert(validator.validArchetype(data), "Marksman should be valid with signature weapon")
  }

  // HEALTH VALIDATION TESTS
  test("Health validation - with health set") {
    val data = createTestCharacterData(h = Some(15))
    val validator = CharacterValidator(data)
    assert(validator.validHealth(data), "Should be valid when health is set")
  }

  test("Health validation - without health set") {
    val data = createTestCharacterData(h = None)
    val validator = CharacterValidator(data)
    assert(!validator.validHealth(data), "Should be invalid when health is not set")
  }

  // INTEGRATION TESTS
  test("Full character validation - valid complete character") {
    val bg = Background.upperClass
    val pt = Personality.loneWolf
    val bgQualities = List((Quality.fitness, d(10)), (Quality.alertness, d(8)))
    val ptQualities = List((Quality.personalityQuality("Leadership"), d(8)))
    val principles = List(samplePrinciple)

    val data = createTestCharacterData(
      bg = Some(bg),
      pt = Some(pt),
      h = Some(20),
      qualityStagingParam = Map(
        bg -> bgQualities,
        pt -> ptQualities
      ),
      abilityStagingParam = Map(bg -> principles)
    )

    val validator = CharacterValidator(data)
    assert(validator.validBackground(data), "Background should be valid")
    assert(validator.validPersonality(data), "Personality should be valid")
    assert(validator.validHealth(data), "Health should be valid")
  }

  test("Character validation with missing components") {
    val data = createTestCharacterData() // No selections
    val validator = CharacterValidator(data)

    assert(!validator.validBackground(data), "Should fail without background")
    assert(!validator.validPersonality(data), "Should fail without personality")
    assert(!validator.validHealth(data), "Should fail without health")
  }

end CharacterValidatorComprehensiveSpec