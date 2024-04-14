package scrpgHelper.chargen

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
  def valid(): Boolean = false
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
    Shadow.shadow, //TODO: Issues with quality & power restrictions together
    //PhysicalPowerhouse.physicalPowerhouse, //TODO: allow powers at different statuses
    Marksman.marksman,
    Blaster.blaster, //TODO: restrict EnergyChoice to available energy powers? Or PowerChoice that doesn't affect dupes?
    //CloseQuartersCombatant.closeQuartersCombatant, //TODO: allow powers at different statuses
    //Armored.armored, //TODO: change how health is calculated
    //Flyer.flyer, //TODO: allow powers at different statuses
    ElementalManipulator.elementalManipulator, //TODO: restrict EnergyChoice to available energy powers? Or PowerChoice that doesn't affect dupes?
    //RobotCyborg.robotCyborg, //TODO: allow powers at different statuses
    Sorcerer.sorcerer,
    Psychic.psychic, //TODO: fix single-power selection bug
    //Transporter.transporter, //TODO: allow powers at different statuses
    //MinionMaker.minionMaker,
    WildCard.wildCard,
    //FormChanger.formChanger,
    Gadgeteer.gadgeteer,
    RealityShaper.realityShaper,
    //Divided.divided,
    //Modular.modular,
  )
end Archetype
