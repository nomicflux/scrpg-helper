package scrpgHelper.chargen

import munit.FunSuite
import com.raquo.laminar.api.L.{*, given}
import scrpgHelper.chargen.characterModel.*

class CharacterValidatorSpec extends FunSuite:

  test("CharacterValidator implements CharacterValidation interface"):
    val mockState = MockCharacterState.minimal
    val validator = CharacterValidator(mockState.data)

    // Verify it implements the interface
    val validationInterface: CharacterValidation = validator
    assert(validationInterface.validBackground != null, "CharacterValidation interface should be implemented")

  test("CharacterValidator combines signals from CharacterState and CharacterStaging"):
    val mockState = MockCharacterState.basicSelections
    val validator = CharacterValidator(mockState.data)

    // Verify all validation signals exist
    assert(validator.validBackground != null, "validBackground signal should exist")
    assert(validator.validPowerSource != null, "validPowerSource signal should exist")
    assert(validator.validArchetype != null, "validArchetype signal should exist")
    assert(validator.validPersonality != null, "validPersonality signal should exist")
    assert(validator.validRedAbilities != null, "validRedAbilities signal should exist")
    assert(validator.validHealth != null, "validHealth signal should exist")

  test("CharacterValidator works with different mock configurations"):
    val minimal = MockCharacterState.minimal
    val basic = MockCharacterState.basicSelections
    val withPowers = MockCharacterState.withPowersAndQualities

    val validatorMinimal = CharacterValidator(minimal.data)
    val validatorBasic = CharacterValidator(basic.data)
    val validatorWithPowers = CharacterValidator(withPowers.data)

    // All should create valid validators
    assert(validatorMinimal.validBackground != null, "minimal validator should work")
    assert(validatorBasic.validPowerSource != null, "basic validator should work")
    assert(validatorWithPowers.validHealth != null, "powers validator should work")

    // All should implement CharacterValidation
    assert(validatorMinimal.isInstanceOf[CharacterValidation], "minimal should implement CharacterValidation")
    assert(validatorBasic.isInstanceOf[CharacterValidation], "basic should implement CharacterValidation")
    assert(validatorWithPowers.isInstanceOf[CharacterValidation], "powers should implement CharacterValidation")

end CharacterValidatorSpec