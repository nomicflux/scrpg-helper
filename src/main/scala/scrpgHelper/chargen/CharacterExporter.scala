package scrpgHelper.chargen

import com.raquo.laminar.api.L.{*, given}

/** Character exporter implementation - exports character state to CharacterModelExport.
  *
  * Takes a CharacterState and implements CharacterExport by combining all the
  * character data into a structured export format.
  */
class CharacterExporter(characterState: CharacterState) extends CharacterExport:

  val forExport: Signal[CharacterModelExport] =
    characterState.background.signal
      .combineWith(
        characterState.powerSource.signal,
        characterState.archetype.signal,
        characterState.personality.signal,
        characterState.health.signal,
        characterState.allPowers,
        characterState.allQualities,
        characterState.allAbilities
      )
      .map { (bg, ps, at, pt, h, pows, quals, abils) =>
        CharacterModelExport(
          bg.map(_.name),
          ps.map(_.name),
          at.map(_.name),
          pt.map(_.name),
          pt.fold(Map())(_.statusDice),
          h,
          pows,
          quals,
          abils.collect { case ca: ChosenAbility => ca },
          abils.collect { case p: Principle => p }
        )
      }

end CharacterExporter