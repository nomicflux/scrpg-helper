package scrpgHelper.chargen.characterModel

import scrpgHelper.chargen.*

/** Character export data interface - pure export functionality.
  *
  * This trait provides pure functions for exporting character data without
  * reactive framework dependencies. All methods are synchronous and return
  * computed values directly.
  */
trait CharacterExportData:

  /** Export character data to structured format.
    *
    * @param data The character data to export
    * @return Complete character export with all relevant information
    */
  def exportData(data: CharacterData): CharacterModelExport

end CharacterExportData