package scrpgHelper.chargen

import com.raquo.laminar.api.L.{*, given}

/** Character export interface - export functionality.
  *
  * This trait captures the export signal from CharacterModel.
  */
trait CharacterExport:

  // Export signal
  val forExport: Signal[CharacterModelExport]

end CharacterExport