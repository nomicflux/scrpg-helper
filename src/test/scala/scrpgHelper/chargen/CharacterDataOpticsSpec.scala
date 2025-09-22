package scrpgHelper.chargen

import scrpgHelper.chargen.characterModel.*
import scrpgHelper.rolls.Die
import scrpgHelper.status.Status
import monocle.syntax.all.*

class CharacterDataOpticsSpec extends munit.FunSuite:
  import Die.d
  import Quality.{alertness, conviction}

  // Create a mock CharacterData for testing
  def createMockData(
      bg: Option[Background] = None,
      ps: Option[PowerSource] = None,
      at: Option[Archetype] = None,
      pt: Option[Personality] = None,
      h: Option[Int] = None,
      qualities: List[(Quality, Die)] = List(),
      powers: List[(Power, Die)] = List(),
      abilities: List[Ability[_]] = List()
  ): CharacterData = new CharacterData {
    def background = bg
    def powerSource = ps
    def archetype = at
    def personality = pt
    def health = h
    def allQualities = qualities
    def allPowers = powers
    def allStagedAbilities = abilities.collect { case ca: ChosenAbility => ca }
    def allChosenAbilities = List()
    def allPrinciples = abilities.collect { case p: Principle => p }
    def allAbilities = abilities
    def redZoneHealth = pt.flatMap(_.statusDice.get(Status.Red)).map(_.n)
    def powerQualityHealth = 4
    def qualityStaging = Map()
    def powerStaging = Map()
    def abilityStaging = Map()
    def abilityChoice = Map()
    def dieChanges = Map()
    def baseAbilities = Map()
  }

  test("CharacterData optics - background access") {
    val data = createMockData(bg = Some(Background.created))

    // Test getting background using optics
    val bg = CharacterData.background.getOption(data)
    assertEquals(bg, Some(Background.created))

    // Test setting background using optics
    val updatedData = CharacterData.background.replace(Background.upperClass)(data)
    assertEquals(CharacterData.background.getOption(updatedData), Some(Background.upperClass))
  }

  test("CharacterData optics - qualities by category") {
    val qualities = List(
      (Quality.alertness, d(6)),
      (Quality.conviction, d(8)),
      (Quality.fitness, d(4))
    )
    val data = createMockData(qualities = qualities)

    // Test getting qualities by category using optics
    val mentalQualities = CharacterData.qualitiesByCategory(QualityCategory.Mental).getAll(data)
    assertEquals(mentalQualities.length, 2) // alertness and conviction are mental

    val physicalQualities = CharacterData.qualitiesByCategory(QualityCategory.Physical).getAll(data)
    assertEquals(physicalQualities.length, 1) // fitness is physical
  }

  test("CharacterData optics - powers by category") {
    val powers = List(
      (Power.agility, d(6)),
      (Power.deduction, d(8))
    )
    val data = createMockData(powers = powers)

    // Test getting powers by category using optics
    val athleticPowers = CharacterData.powersByCategory(PowerCategory.Athletic).getAll(data)
    assertEquals(athleticPowers.length, 1)

    val intellectualPowers = CharacterData.powersByCategory(PowerCategory.Intellectual).getAll(data)
    assertEquals(intellectualPowers.length, 1)
  }

  test("CharacterComputation optics utilities") {
    val qualities = List(
      (Quality.alertness, d(6)),
      (Quality.conviction, d(8)),
      (Quality.fitness, d(4))
    )
    val powers = List(
      (Power.agility, d(6)),
      (Power.deduction, d(8))
    )
    val data = createMockData(qualities = qualities, powers = powers)

    // Test optics-based utility functions
    val mentalQualities = CharacterComputation.getQualitiesByCategory(data, QualityCategory.Mental)
    assertEquals(mentalQualities.length, 2)

    val athleticPowers = CharacterComputation.getPowersByCategory(data, PowerCategory.Athletic)
    assertEquals(athleticPowers.length, 1)

    // Test counting by category
    val qualityCounts = CharacterComputation.countQualitiesByCategory(data)
    assertEquals(qualityCounts(QualityCategory.Mental), 2)
    assertEquals(qualityCounts(QualityCategory.Physical), 1)

    // Test highest die for category
    val highestMentalDie = CharacterComputation.getHighestDieForQualityCategory(data, QualityCategory.Mental)
    assertEquals(highestMentalDie, Some(d(8))) // conviction has d(8)
  }

  test("CharacterComputation die changes with optics") {
    val qualities = List(
      (Quality.alertness, d(6)),
      (Quality.conviction, d(8))
    )
    val data = createMockData(qualities = qualities)

    val dieChanges = Map[Quality | Power, CharacterComputation.DieChange](
      Quality.alertness -> CharacterComputation.DieChange.Upgrade
    )

    // Test applying die changes using optics
    val modifiedQualities = CharacterComputation.applyDieChangesToQualities(data, dieChanges)

    // Find the modified alertness quality
    val modifiedAlertness = modifiedQualities.find(_._1 == Quality.alertness)
    assertEquals(modifiedAlertness.map(_._2), Some(d(8))) // d(6) upgraded to d(8)

    // Conviction should remain unchanged
    val unchangedConviction = modifiedQualities.find(_._1 == Quality.conviction)
    assertEquals(unchangedConviction.map(_._2), Some(d(8)))
  }

end CharacterDataOpticsSpec