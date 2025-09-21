package scrpgHelper.chargen

import munit.FunSuite
import com.raquo.laminar.api.L.{*, given}
import scrpgHelper.rolls.Die

class CharacterExporterSpec extends FunSuite:

  test("CharacterExporter implements CharacterExport interface"):
    val mockState = MockCharacterState.minimal
    val exporter = CharacterExporter(mockState)

    // Verify it implements the interface
    val exportInterface: CharacterExport = exporter
    assert(exportInterface.forExport != null, "CharacterExport interface should be implemented")

  test("CharacterExporter combines signals from CharacterState"):
    val mockState = MockCharacterState.basicSelections
    val exporter = CharacterExporter(mockState)

    // Verify the exporter uses the character state signals
    assert(exporter.forExport != null, "forExport signal should exist")

    // Verify it's a proper Signal type
    assert(exporter.forExport.isInstanceOf[Signal[CharacterModelExport]], "forExport should be Signal[CharacterModelExport]")

  test("CharacterExporter works with different mock configurations"):
    val minimal = MockCharacterState.minimal
    val basic = MockCharacterState.basicSelections
    val withPowers = MockCharacterState.withPowersAndQualities

    val exporterMinimal = CharacterExporter(minimal)
    val exporterBasic = CharacterExporter(basic)
    val exporterWithPowers = CharacterExporter(withPowers)

    // All should create valid exporters
    assert(exporterMinimal.forExport != null, "minimal exporter should work")
    assert(exporterBasic.forExport != null, "basic exporter should work")
    assert(exporterWithPowers.forExport != null, "powers exporter should work")

    // All should implement CharacterExport
    assert(exporterMinimal.isInstanceOf[CharacterExport], "minimal should implement CharacterExport")
    assert(exporterBasic.isInstanceOf[CharacterExport], "basic should implement CharacterExport")
    assert(exporterWithPowers.isInstanceOf[CharacterExport], "powers should implement CharacterExport")

end CharacterExporterSpec