package scrpgHelper.chargen

import com.raquo.laminar.api.L.{*, given}
import scrpgHelper.chargen.characterModel.*

/** Character exporter implementation - exports character data to CharacterModelExport.
  *
  * Takes CharacterData and implements both CharacterExportData (pure) and
  * CharacterExport (reactive) by delegating to pure computation logic.
  */
class CharacterExporter(characterData: CharacterData) extends CharacterExportData with CharacterExport:

  // Pure data implementation
  def exportData(data: CharacterData): CharacterModelExport =
    CharacterExportComputation.computeExport(data)

  // Reactive implementation - deterministic view disguised as signal (temporary for API compatibility)
  val forExport: Signal[CharacterModelExport] = Signal.fromValue(exportData(characterData))

end CharacterExporter