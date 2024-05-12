package scrpgHelper.chargen.powers

object Cursed:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.PowerSource.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val cursed: PowerSource = PowerSource(
    "Cursed",
    13,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Attunement",
            Status.Yellow,
            AbilityCategory.Inherent,
            _ => List(Action.Recover),
            List(
              "When you would take damage from ",
              EnergyChoice(),
              ", you may Recover that amount of Health instead."
            ),
          ),
          AbilityTemplate(
            "Costly Strength",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Boost, Action.Hinder),
            List(
              "Boost all nearby allies using ",
              PowerChoice(AbilityChoice.noDupes),
              ". Use your Max+Mid die. Hinder yourself with your Min die.",
            ),
          ),
          AbilityTemplate(
            "Cursed Resolve",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Boost, Action.Recover),
            List(
              "Boost yourself using ",
              PowerChoice(AbilityChoice.noDupes),
              ". Then, either remove a penalty on yourself or Recover using your Min die.",
            ),
          ),
        ),
      ),
      AbilityPool(
        1,
        List(
          AbilityTemplate(
            "Double Edged Luck",
            Status.Green,
            AbilityCategory.Inherent,
            _ => List(),
            List(
              "Whenever you roll a 1 on one or more dice, you may reroll those dice. You must accept the result of the reroll."
            ),
          ),
          AbilityTemplate(
            "Extremes",
            Status.Green,
            AbilityCategory.Inherent,
            _ => List(),
            List(
              "Whenever you roll a die's max value, treat that value as 1 higher. When you roll a 1 on a die, treat that die as if it had rolled a 0."
            ),
          )
        ),
      )
    ),
    List(Power.signatureWeapon) ++ Power.athleticPowers ++ Power.energyPowers ++ Power.materialPowers ++ Power.technologicalPowers,
    List(d(12), d(6))
  )
end Cursed
