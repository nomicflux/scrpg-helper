package scrpgHelper.chargen

case class AbilityPool(max: Int,
                       abilities: List[AbilityTemplate],
                       validation: List[ChosenAbility] => Boolean)
