package scrpgHelper.chargen.powers

object Genetic:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.PowerSource.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val genetic: PowerSource = PowerSource(
    "Genetic",
    3,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Danger Sense",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(Action.Defend),
            List(
              "When damaged by an environment target or a surprise Attack, Defend by rolling your single ",
              PowerChoice(AbilityChoice.noDupes),
              " die."
            ),
          ),
          AbilityTemplate(
            "Adaptive",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Boost, Action.Recover),
            List(
              "Boost yourself using ",
              PowerChoice(AbilityChoice.noDupes),
              ", then either remove a penalty on yourself or Recover using your Min die.",
            ),
          ),
          AbilityTemplate(
            "Area Assault",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack multiple targets using ",
              PowerChoice(AbilityChoice.noDupes),
              ", using your Min die against each.",
            ),
          ),
        ),
      ),
      AbilityPool(
        1,
        List(
          AbilityTemplate(
            "Growth",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Boost),
            List(
              "Boost yourself using ",
              QualityChoice(),
              ". That bonus is persistent and exclusive.",
            ),
          ),
          AbilityTemplate(
            "Rally",
            Status.Green,
            AbilityCategory.Action,
            _ => List(Action.Attack, Action.Recover),
            List(
              "Attack using ",
              QualityChoice(),
              ". Other nearby heroes in the Yellow or Red zone Recover equal to your Min die."
            ),
          )
        ),
      )
    ),
    List(Power.agility, Power.flight, Power.signatureWeapon, Power.strength, Power.vitality) ++ Power.intellectualPowers ++ Power.psychicPowers,
    List(d(10), d(8), d(8))
  )
end Genetic
