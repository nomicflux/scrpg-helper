package scrpgHelper.chargen

import munit.FunSuite
import com.raquo.laminar.api.L.{*, given}
import scrpgHelper.chargen.characterModel.*

class CharacterValidatorSpec extends FunSuite:

  test("CharacterValidator can be created with CharacterData"):
    val mockState = MockCharacterState.minimal
    val validator = CharacterValidator(mockState.data)
    assert(validator != null, "CharacterValidator should be created successfully")

  test("CharacterValidator provides pure validation functions"):
    val mockState = MockCharacterState.basicSelections
    val validator = CharacterValidator(mockState.data)

    // Verify all validation functions exist and return boolean values
    assert(validator.validBackground(mockState.data).isInstanceOf[Boolean], "validBackground should return Boolean")
    assert(validator.validPowerSource(mockState.data).isInstanceOf[Boolean], "validPowerSource should return Boolean")
    assert(validator.validArchetype(mockState.data).isInstanceOf[Boolean], "validArchetype should return Boolean")
    assert(validator.validPersonality(mockState.data).isInstanceOf[Boolean], "validPersonality should return Boolean")
    assert(validator.validRedAbilities(mockState.data).isInstanceOf[Boolean], "validRedAbilities should return Boolean")
    assert(validator.validHealth(mockState.data).isInstanceOf[Boolean], "validHealth should return Boolean")

  test("CharacterValidator works with different mock configurations"):
    val minimal = MockCharacterState.minimal
    val basic = MockCharacterState.basicSelections
    val withPowers = MockCharacterState.withPowersAndQualities

    val validatorMinimal = CharacterValidator(minimal.data)
    val validatorBasic = CharacterValidator(basic.data)
    val validatorWithPowers = CharacterValidator(withPowers.data)

    // All should create valid validators and return boolean results
    assert(validatorMinimal.validBackground(minimal.data).isInstanceOf[Boolean], "minimal validator should work")
    assert(validatorBasic.validPowerSource(basic.data).isInstanceOf[Boolean], "basic validator should work")
    assert(validatorWithPowers.validHealth(withPowers.data).isInstanceOf[Boolean], "powers validator should work")

end CharacterValidatorSpec