package scrpgHelper.chargen.characterModel

import scrpgHelper.chargen.*

/** Character export computation - pure functions for computing export data.
  *
  * This object contains all the business logic for computing character export
  * data from character state. All methods are pure functions that take explicit
  * parameters and return computed results without side effects.
  */
object CharacterExportComputation:

  /** Compute complete character export from character data.
    *
    * @param data The character data to export
    * @return Complete structured export with all character information
    */
  def computeExport(data: CharacterData): CharacterModelExport =
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

end CharacterExportComputation