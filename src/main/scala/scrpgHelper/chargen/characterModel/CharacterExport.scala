package scrpgHelper.chargen.characterModel

import com.raquo.laminar.api.L.{*, given}
import scrpgHelper.chargen.*

/** Character export interface - export functionality.
  *
  * This trait captures the export signal from CharacterModel.
  */
trait CharacterExport:

  // Export signal
  val forExport: Signal[CharacterModelExport]

end CharacterExport