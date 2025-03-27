package scrpgHelper.chargen

import com.raquo.laminar.api.L.{*, given}
import scrpgHelper.rolls.Die
import scrpgHelper.status.Status
import scrpgHelper.chargen.{Background, PowerSource, Archetype, AbilityPool, PrincipleCategory, Power, Quality, ChosenAbility, Principle, Personality, QualityCategory, AbilityCategory, AbilityTemplate, PowerCategory, AbilityKey}
import scrpgHelper.chargen.powers.Accident
import scrpgHelper.chargen.archetypes.Gadgeteer
import scrpgHelper.chargen.Background.created
import com.raquo.airstream.core.Observer
import com.raquo.airstream.state.Var

class CharacterModelTest extends munit.FunSuite:
  import Die.d
  import Quality.{alertness, conviction}
  import PrincipleCategory.*

  test("CharacterModel basic attribute management") {
    val model = new CharacterModel()
    
    // Test background
    model.changeBackground.onNext(created)
    assertEquals(model.background.now(), Some(created))
    
    // Test power source
    model.changePowerSource.onNext(Accident.accident)
    assertEquals(model.powerSource.now(), Some(Accident.accident))
    
    // Test archetype
    model.changeArchetype.onNext(Gadgeteer.gadgeteer)
    assertEquals(model.archetype.now(), Some(Gadgeteer.gadgeteer))
  }

  test("CharacterModel basic signal values") {
    val model = new CharacterModel()
    
    // Setup basic character data
    model.changeBackground.onNext(created)
    model.changePowerSource.onNext(Accident.accident)
    model.changeArchetype.onNext(Gadgeteer.gadgeteer)
    
    // Test signals
    assertEquals(model.background.now(), Some(created))
    assertEquals(model.powerSource.now(), Some(Accident.accident))
    assertEquals(model.archetype.now(), Some(Gadgeteer.gadgeteer))
  }

  test("CharacterModel personality management") {
    val model = new CharacterModel()
    
    // Initial personality should be None
    assertEquals(model.personality.now(), None)
    
    // Set personality
    model.changePersonality.onNext(Personality.loneWolf)
    assertEquals(model.personality.now(), Some(Personality.loneWolf))
    
    // Verify base quality is added
    val loneWolfQualities = model.qualityStaging.now()(Personality.loneWolf)
    assertEquals(loneWolfQualities.length, 1)
    assertEquals(loneWolfQualities.head._1, Personality.loneWolf.baseQuality)
    assertEquals(loneWolfQualities.head._2, Die.d(8))
    
    // Test changing to a different personality
    model.changePersonality.onNext(Personality.naturalLeader)
    assertEquals(model.personality.now(), Some(Personality.naturalLeader))
    
    // Verify the new personality's qualities
    val leaderQualities = model.qualityStaging.now()(Personality.naturalLeader)
    assertEquals(leaderQualities.length, 1)
    assertEquals(leaderQualities.head._1, Personality.naturalLeader.baseQuality)
    assertEquals(leaderQualities.head._2, Die.d(8))  
  }

  test("CharacterModel quality staging") {
    val model = new CharacterModel()
    model.changeBackground.onNext(created)
    
    // Add quality
    model.addQuality(created).onNext((Quality.alertness, Die.d(6)))
    val qualities = model.qualityStaging.now()(created)
    assertEquals(qualities.length, 1)
    assertEquals(qualities.head, (Quality.alertness, Die.d(6)))
    
    // Remove quality
    model.removeQuality(created).onNext((Quality.alertness, Die.d(6)))
    assertEquals(model.qualityStaging.now()(created).length, 0)
    
    // Prevent duplicate qualities
    model.addQuality(created).onNext((Quality.alertness, Die.d(6)))
    model.addQuality(created).onNext((Quality.alertness, Die.d(8)))
    assertEquals(model.qualityStaging.now()(created).length, 2)
  }

  test("CharacterModel power staging") {
    val model = new CharacterModel()
    model.changePowerSource.onNext(Accident.accident)
    
    // Add power
    val testPower = Accident.accident.powerList.head
    model.addPower(Accident.accident).onNext((testPower, Die.d(6)))
    val powers = model.powerStaging.now()(Accident.accident)
    assertEquals(powers.length, 1)
    assertEquals(powers.head._1, testPower)
    
    // Remove power
    model.removePower(Accident.accident).onNext((testPower, Die.d(6)))
    assertEquals(model.powerStaging.now()(Accident.accident).length, 0)
  }

  test("CharacterModel ability staging and validation") {
    val model = new CharacterModel()
    model.changeBackground.onNext(created)
    
    // Create a test ability
    val testPool = AbilityPool(1, List())
    val testAbility = AbilityTemplate(
      new AbilityId(),
      "Test Ability", 
      Status.Green, 
      AbilityCategory.Action,
      _ => List(),
      List("Test")
    ).toChosenAbility(testPool)
    
    // Toggle ability on
    model.toggleAbility(created).onNext(testAbility)
    val abilities = model.abilityStaging.now()(created)
    assertEquals(abilities.length, 1)
    assertEquals(abilities.head.key, testAbility.key)
    
    // Toggle ability off
    model.toggleAbility(created).onNext(testAbility)
    assertEquals(model.abilityStaging.now()(created).length, 0)
    
    // Test ability selected signal
    model.toggleAbility(created).onNext(testAbility)
    val selected = model.abilityStaging.now()(created).exists(_.key == testAbility.key)
    assertEquals(selected, true)
  }

  test("CharacterModel personality management") {
    val model = new CharacterModel()
    
    // Initial personality should be None
    assertEquals(model.personality.now(), None)
    
    // Set personality
    model.changePersonality.onNext(Personality.naturalLeader)
    assertEquals(model.personality.now(), Some(Personality.naturalLeader))
    
    // Get qualities from personality
    val leaderQualities = model.qualityStaging.now()(Personality.naturalLeader)
    assertEquals(leaderQualities.length, 1)
    assertEquals(leaderQualities.head._1, Personality.naturalLeader.baseQuality)
    assertEquals(leaderQualities.head._2, Die.d(8))  // naturalLeader has List(d(6), d(8), d(10))
    
    // Verify status dice through personality
    assertEquals(model.personality.now().flatMap(_.statusDice.get(Status.Green)), Some(d(6)))
    assertEquals(model.personality.now().flatMap(_.statusDice.get(Status.Yellow)), Some(d(8)))
    assertEquals(model.personality.now().flatMap(_.statusDice.get(Status.Red)), Some(d(10)))
  }

  test("CharacterModel quality staging") {
    val model = new CharacterModel()
    model.changeBackground.onNext(created)
    
    // Add quality
    model.addQuality(created).onNext((Quality.alertness, Die.d(6)))
    val qualities = model.qualityStaging.now()(created)
    assertEquals(qualities.length, 1)
    assertEquals(qualities.head, (Quality.alertness, Die.d(6)))
    
    // Remove quality
    model.removeQuality(created).onNext((Quality.alertness, Die.d(6)))
    assertEquals(model.qualityStaging.now()(created).length, 0)
    
    // Prevent duplicate qualities
    model.addQuality(created).onNext((Quality.alertness, Die.d(6)))
    model.addQuality(created).onNext((Quality.alertness, Die.d(8)))
    assertEquals(model.qualityStaging.now()(created).length, 2)
  }

  test("CharacterModel power staging") {
    val model = new CharacterModel()
    model.changePowerSource.onNext(Accident.accident)
    
    // Add power
    val testPower = Accident.accident.powerList.head
    model.addPower(Accident.accident).onNext((testPower, Die.d(6)))
    val powers = model.powerStaging.now()(Accident.accident)
    assertEquals(powers.length, 1)
    assertEquals(powers.head._1, testPower)
    
    // Remove power
    model.removePower(Accident.accident).onNext((testPower, Die.d(6)))
    assertEquals(model.powerStaging.now()(Accident.accident).length, 0)
  }

  test("CharacterModel ability staging and validation") {
    val model = new CharacterModel()
    model.changeBackground.onNext(created)
    
    // Create a test ability
    val testPool = AbilityPool(1, List())
    val testAbility = AbilityTemplate(
      new AbilityId(),
      "Test Ability", 
      Status.Green, 
      AbilityCategory.Action,
      _ => List(),
      List("Test")
    ).toChosenAbility(testPool)
    
    // Toggle ability on
    model.toggleAbility(created).onNext(testAbility)
    val abilities = model.abilityStaging.now()(created)
    assertEquals(abilities.length, 1)
    assertEquals(abilities.head.key, testAbility.key)
    
    // Toggle ability off
    model.toggleAbility(created).onNext(testAbility)
    assertEquals(model.abilityStaging.now()(created).length, 0)
    
    // Test ability selected signal
    model.toggleAbility(created).onNext(testAbility)
    val selected = model.abilityStaging.now()(created).exists(_.key == testAbility.key)
    assertEquals(selected, true)
  }
