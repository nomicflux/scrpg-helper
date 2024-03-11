package scrpgHelper.chargen

enum PowerCategory:
  case Athletic, Energy, Hallmark, Intellectual, Material, SelfControl, Psychic,
    Mobility, Technological
end PowerCategory

final case class Power(name: String, category: PowerCategory)

object Power:
  import PowerCategory.*

  def apply(name: String, category: PowerCategory): Power =
    new Power(name, category)
  end apply

  val agility = Power("Agility", Athletic)
  val speed = Power("Speed", Athletic)
  val strength = Power("Strength", Athletic)
  val vitality = Power("Vitality", Athletic)
  val athleticPowers: List[Power] =
    List(agility, speed, strength, vitality)

  val cold = Power("Cold", Energy)
  val cosmic = Power("Cosmic", Energy)
  val electricity = Power("Electricity", Energy)
  val fire = Power("Fire", Energy)
  val infernal = Power("Infernal", Energy)
  val nuclear = Power("Nuclear", Energy)
  val radiant = Power("Radiant", Energy)
  val sonic = Power("Sonic", Energy)
  val weather = Power("Weather", Energy)
  val energyPowers: List[Power] =
    List(
      cold,
      cosmic,
      electricity,
      fire,
      infernal,
      nuclear,
      radiant,
      sonic,
      weather
    )

  val signatureVehicle = Power("Signature Vehicle", Hallmark)
  val signatureWeapon = Power("Signature Weapon", Hallmark)
  val hallmarkPowers: List[Power] =
    List(signatureVehicle, signatureWeapon)

  val awareness = Power("Awareness", Intellectual)
  val deduction = Power("Deduction", Intellectual)
  val intuition = Power("Intuition", Intellectual)
  val lightningCalculator = Power("Lightning Calculator", Intellectual)
  val presence = Power("Presence", Intellectual)
  val intellectualPowers: List[Power] =
    List(awareness, deduction, intuition, lightningCalculator, presence)

  val metal = Power("Metal", Material)
  val plants = Power("Plants", Material)
  val stone = Power("Stone", Material)
  val toxic = Power("Toxic", Material)
  val transmutation = Power("Transmutation", Material)
  val materialPowers: List[Power] =
    List(metal, plants, stone, toxic, transmutation)

  val absorption = Power("Absorption", SelfControl)
  val densityControl = Power("Density Control", SelfControl)
  val duplication = Power("Duplication", SelfControl)
  val elasticity = Power("Elasticity", SelfControl)
  val intangibility = Power("Intangibility", SelfControl)
  val invisibility = Power("Invisibility", SelfControl)
  val partDetachment = Power("Part Detachment", SelfControl)
  val shapeshifting = Power("Shapeshifting", SelfControl)
  val sizeChanging = Power("Size-Changing", SelfControl)
  val selfControlPowers: List[Power] =
    List(
      absorption,
      densityControl,
      duplication,
      elasticity,
      intangibility,
      invisibility,
      partDetachment,
      shapeshifting,
      sizeChanging
    )

  val animalControl = Power("Animal Control", Psychic)
  val illusions = Power("Illusions", Psychic)
  val precognition = Power("Precognition", Psychic)
  val postcognition = Power("Postcognition", Psychic)
  val remoteViewing = Power("Remote Viewing", Psychic)
  val suggestion = Power("Suggestion", Psychic)
  val telekinesis = Power("Telekinesis", Psychic)
  val telepathy = Power("Telepathy", Psychic)
  val psychicPowers: List[Power] =
    List(
      animalControl,
      illusions,
      precognition,
      postcognition,
      remoteViewing,
      suggestion,
      telekinesis,
      telepathy
    )

  val flight = Power("Flight", Mobility)
  val leaping = Power("Leaping", Mobility)
  val momentum = Power("Momentum", Mobility)
  val swimming = Power("Swimming", Mobility)
  val swinging = Power("Swinging", Mobility)
  val teleportation = Power("Teleportation", Mobility)
  val wallCrawling = Power("Wall-Crawling", Mobility)
  val mobilityPowers: List[Power] =
    List(
      flight,
      leaping,
      momentum,
      swimming,
      swinging,
      teleportation,
      wallCrawling
    )

  val gadgets = Power("Gadgets", Technological)
  val inventions = Power("Inventions", Technological)
  val powerSuit = Power("Power Suit", Technological)
  val robotics = Power("Robotics", Technological)
  val technologicalPowers: List[Power] =
    List(gadgets, inventions, powerSuit, robotics)
end Power
