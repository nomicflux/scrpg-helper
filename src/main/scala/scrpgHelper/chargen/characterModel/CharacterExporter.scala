package scrpgHelper.chargen.characterModel

import scrpgHelper.chargen.characterModel.*
import scrpgHelper.chargen.{CharacterModelExport, ChosenAbility, Principle}

/** Character exporter implementation - exports character data to CharacterModelExport.
  *
  * Takes CharacterData and provides pure export functions.
  * No more signals - just pure export logic.
  */
class CharacterExporter(characterData: CharacterData):

  def exportData(data: CharacterData): CharacterModelExport =
    CharacterModelExport(
      background = data.background.map(_.name),
      powerSource = data.powerSource.map(_.name),
      archetype = data.archetype.map(_.name),
      personality = data.personality.map(_.name),
      statuses = data.personality.fold(Map())(_.statusDice),
      health = data.health,
      powers = data.allPowers,
      qualities = data.allQualities,
      abilities = data.allAbilities.collect { case ca: ChosenAbility => ca },
      principles = data.allAbilities.collect { case p: Principle => p }
    )

end CharacterExporter