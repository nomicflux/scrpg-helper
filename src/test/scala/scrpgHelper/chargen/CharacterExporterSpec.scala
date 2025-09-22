package scrpgHelper.chargen

import munit.FunSuite
import com.raquo.laminar.api.L.{*, given}
import scrpgHelper.rolls.Die
import scrpgHelper.chargen.characterModel.*

class CharacterExporterSpec extends FunSuite:

  test("CharacterExporter can be created with CharacterData"):
    val mockState = MockCharacterState.minimal
    val exporter = CharacterExporter(mockState.data)
    assert(exporter != null, "CharacterExporter should be created successfully")

  test("CharacterExporter provides pure export functions"):
    val mockState = MockCharacterState.basicSelections
    val exporter = CharacterExporter(mockState.data)

    // Verify the exporter provides pure export function
    val exportResult = exporter.exportData(mockState.data)
    assert(exportResult != null, "exportData should return a result")
    assert(exportResult.isInstanceOf[CharacterModelExport], "exportData should return CharacterModelExport")

  test("CharacterExporter works with different mock configurations"):
    val minimal = MockCharacterState.minimal
    val basic = MockCharacterState.basicSelections
    val withPowers = MockCharacterState.withPowersAndQualities

    val exporterMinimal = CharacterExporter(minimal.data)
    val exporterBasic = CharacterExporter(basic.data)
    val exporterWithPowers = CharacterExporter(withPowers.data)

    // All should create valid exporters and return export data
    assert(exporterMinimal.exportData(minimal.data) != null, "minimal exporter should work")
    assert(exporterBasic.exportData(basic.data) != null, "basic exporter should work")
    assert(exporterWithPowers.exportData(withPowers.data) != null, "powers exporter should work")

end CharacterExporterSpec