package scrpgHelper.chargen.powers

object Accident:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.PowerSource.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val accident: PowerSource = PowerSource(
    "Accident",
    1,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Area Alteration",
            Status.Yellow,
            AbilityCategory.Action,
            choices => choices.flatMap(_.getAction.toList),
            choices =>
              s"${AbilityChoice.actionString(choices)} any number of nearby targets using ${AbilityChoice.powerString(choices)}. Use your Max die.",
            List(
              ActionChoice(actions =>
                actions
                  .map(Set(Action.Boost, Action.Hinder).contains(_))
                  .foldLeft(true)(_ && _)
              ),
              PowerChoice()
            )
          ),
          AbilityTemplate(
            "Inflict",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Hinder),
            choices =>
              s"Attack using ${AbilityChoice.powerString(choices)}. Hinder that same target using your Min die.",
          List(PowerChoice())),
          AbilityTemplate(
            "Reflexive Burst",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(Action.Attack),
            choices =>
              s"When your personal zone changes, Attack all close enemy targets by rolling your single ${AbilityChoice.powerString(choices)} die.",
          List(PowerChoice()))
        ),
        abilities => true //PowerSource.uniquePowers(abilities)
      ),
      AbilityPool(
        1,
        List(
          AbilityTemplate(
            "Ambush Awareness",
            Status.Green,
            AbilityCategory.Reaction,
            _ => List(Action.Defend),
            choices =>
              s"If you haven't yet acted in an action scene, you may Defend against an Attack by rolling your single ${AbilityChoice.powerString(choices)} die.",
          List(PowerChoice())),
          AbilityTemplate(
            "Change in Circumstance",
            Status.Green,
            AbilityCategory.Reaction,
            _ => List(Action.Boost),
            choices =>
              s"When you change personal zones, you may Boost by rolling your single ${AbilityChoice.powerString(choices)} die.",
          List(PowerChoice())),
          AbilityTemplate(
            "Immunity",
            Status.Green,
            AbilityCategory.Inherent,
            _ => List(),
            choices =>
              s"You do not take damage from ${AbilityChoice.energyString(choices)}",
          List(EnergyChoice()))
        ),
        _ => true
      )
    ),
    Power.athleticPowers ++ Power.energyPowers ++ Power.intellectualPowers ++ Power.materialPowers ++ Power.psychicPowers ++ Power.selfControlPowers,
    List(d(12), d(6))
  )
end Accident
