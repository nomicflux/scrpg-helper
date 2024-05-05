package scrpgHelper.chargen

enum QualityCategory:
  case Information, Mental, Physical, Social, Personal
end QualityCategory

final case class Quality(name: String, category: QualityCategory):
  def changeName(n: String): Quality =
    copy(name = n)
end Quality

object Quality:
  import QualityCategory.*

  def personalityQuality(n: String) =
    Quality(n, Personal)

  def apply(name: String, category: QualityCategory): Quality =
    new Quality(name, category)
  end apply

  val criminalUnderworldInfo = Quality("Criminal Underworld Info", Information)
  val deepSpaceKnowledge = Quality("Deep Space Knowledge", Information)
  val history = Quality("History", Information)
  val magicalLore = Quality("Magical Lore", Information)
  val medicine = Quality("Medicine", Information)
  val otherworldlyMythos = Quality("Otherworldly Mythos", Information)
  val science = Quality("Science", Information)
  val technology = Quality("Technology", Information)
  val informationQualities: List[Quality] =
    List(
      criminalUnderworldInfo,
      deepSpaceKnowledge,
      history,
      magicalLore,
      medicine,
      otherworldlyMythos,
      science,
      technology
    )

  val alertness = Quality("Alertness", Mental)
  val conviction = Quality("Conviction", Mental)
  val creativity = Quality("Creativity", Mental)
  val investigation = Quality("Investigation", Mental)
  val selfDiscipline = Quality("Self-Discipline", Mental)
  val mentalQualities: List[Quality] =
    List(alertness, conviction, creativity, investigation, selfDiscipline)

  val acrobatics = Quality("Acrobatics", Physical)
  val closeCombat = Quality("Close Combat", Physical)
  val finesse = Quality("Finesse", Physical)
  val fitness = Quality("Fitness", Physical)
  val rangedCombat = Quality("Ranged Combat", Physical)
  val stealth = Quality("Stealth", Physical)
  val physicalQualities: List[Quality] =
    List(acrobatics, closeCombat, finesse, fitness, rangedCombat, stealth)

  val banter = Quality("Banter", Social)
  val insight = Quality("Insight", Social)
  val imposing = Quality("Imposing", Social)
  val leadership = Quality("Leadership", Social)
  val persuasion = Quality("Persuasion", Social)
  val socialQualities: List[Quality] =
    List(banter, insight, imposing, leadership, persuasion)
end Quality
