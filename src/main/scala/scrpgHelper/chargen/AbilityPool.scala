package scrpgHelper.chargen

final class AbilityPoolId

case class AbilityPool(
    id: AbilityPoolId,
    max: Int,
    abilities: List[AbilityTemplate],
    validation: List[ChosenAbility] => Boolean
):
  def runValidation(abilities: List[ChosenAbility]): Boolean =
    val sharePool = abilities.filter(_.inPool.id == id)
    sharePool.size <= max && validation(sharePool)
  end runValidation
end AbilityPool

object AbilityPool:
  def apply(
      max: Int,
      abilities: List[AbilityTemplate],
      validation: List[ChosenAbility] => Boolean
  ): AbilityPool =
    new AbilityPool(new AbilityPoolId(), max, abilities, validation)
end AbilityPool
