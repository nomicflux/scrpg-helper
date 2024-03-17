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
            List(
              ActionChoice(actions =>
                actions
                  .map(Set(Action.Boost, Action.Hinder).contains(_))
                  .foldLeft(true)(_ && _)
              ),
              " any number of nearby targets using ",
              PowerChoice(AbilityChoice.noDupes(_)),
              ". Use your Max die."
            ),
          ),
          AbilityTemplate(
            "Inflict",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Hinder),
            List(
              "Attack using ",
              PowerChoice(AbilityChoice.noDupes(_)),
              ". Hinder that same target using your Min die.",
            ),
          ),
          AbilityTemplate(
            "Reflexive Burst",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(Action.Attack),
            List(
              "When your personal zone changes, Attack all close enemy targets by rolling your single ",
              PowerChoice(AbilityChoice.noDupes(_)),
              " die.",
            ),
          ),
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
            List(
              "If you haven't yet acted in an action scene, you may Defend against an Attack by rolling your single ",
              PowerChoice(AbilityChoice.noDupes(_)),
              " die.",
            ),
          ),
          AbilityTemplate(
            "Change in Circumstance",
            Status.Green,
            AbilityCategory.Reaction,
            _ => List(Action.Boost),
            List(
              "When you change personal zones, you may Boost by rolling your single ",
              PowerChoice(AbilityChoice.noDupes(_)),
              " die.",
            ),
          ),
          AbilityTemplate(
            "Immunity",
            Status.Green,
            AbilityCategory.Inherent,
            _ => List(),
            List(
              "You do not take damage from ",
              EnergyChoice(),
            ),
          )
        ),
        _ => true
      )
    ),
    Power.athleticPowers ++ Power.energyPowers ++ Power.intellectualPowers ++ Power.materialPowers ++ Power.psychicPowers ++ Power.selfControlPowers,
    List(d(12), d(6))
  )
end Accident
