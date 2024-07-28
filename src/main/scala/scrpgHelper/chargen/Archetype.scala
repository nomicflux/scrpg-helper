package scrpgHelper.chargen

import scrpgHelper.rolls.Die

case class Archetype(
    name: String,
    number: Int,
    powerValidation: Set[Power] => Boolean,
    qualityValidation: Set[Quality] => Boolean,
    minPowers: Int,
    powerList: List[Power],
    qualityList: List[Quality],
    abilityPools: List[AbilityPool],
    principleCategory: PrincipleCategory
):
  def valid(
      diePool: List[Die],
      powers: List[Power],
      qualities: List[Quality],
      abilities: List[ChosenAbility],
      allPowers: List[Power],
      allQualities: List[Quality],
  ): Boolean =
    abilities.filter(_.valid).size == abilityPools.map(_.max).sum &&
    (powers.size + qualities.size) == diePool.size &&
    powerValidation(allPowers.toSet) && qualityValidation(allQualities.toSet)
  end valid
end Archetype

object Archetype:
  import scrpgHelper.chargen.archetypes.*

  def signaturePower(p: Power): Set[Power] => Boolean =
    ps => ps.contains(p)

  def signaturePowerCategory(pc: PowerCategory): Set[Power] => Boolean =
    ps => !ps.filter(p => p.category == pc).isEmpty

  def signatureQuality(q: Quality): Set[Quality] => Boolean =
    qs => qs.contains(q)

  val archetypes: List[Archetype] = List(
    Speedster.speedster,
    Shadow.shadow,
    PhysicalPowerhouse.physicalPowerhouse,
    Marksman.marksman,
    Blaster.blaster,
    CloseQuartersCombatant.closeQuartersCombatant, // TODO: mandate one power used with abilities; shared no-dupes between zones
    // Armored.armored, //TODO: change how health is calculated
    Flyer.flyer, // TODO: enforce abilities using flight & sig vehicle
    ElementalManipulator.elementalManipulator,
    // RobotCyborg.robotCyborg, //TODO: change how health is calculated
    Sorcerer.sorcerer,
    Psychic.psychic,
    Transporter.transporter,
    // MinionMaker.minionMaker,
    WildCard.wildCard,
    // FormChanger.formChanger,
    Gadgeteer.gadgeteer,
    RealityShaper.realityShaper
    // Divided.divided,
    // Modular.modular,
  )
end Archetype
