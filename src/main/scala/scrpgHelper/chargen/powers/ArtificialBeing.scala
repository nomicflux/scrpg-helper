package scrpgHelper.chargen.powers

object ArtificialBeing:
  import scrpgHelper.chargen.*
  import scrpgHelper.chargen.PowerSource.*
  import scrpgHelper.status.Status
  import scrpgHelper.rolls.Die.d

  val artificialBeing: PowerSource = PowerSource(
    "Artifical Being",
    12,
    List(
      AbilityPool(
        2,
        List(
          AbilityTemplate(
            "Created Immunity",
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
            "Multiple Assault",
            Status.Yellow,
            AbilityCategory.Action,
            _ => List(Action.Attack),
            List(
              "Attack using ",
              PowerChoice(AbilityChoice.noDupes(_)),
              " against multiple targets, using your Min die against each.",
            ),
          ),
          AbilityTemplate(
            "Recalculating...",
            Status.Yellow,
            AbilityCategory.Reaction,
            _ => List(),
            List(
              "After rolling during your turn, you may take 1 irreducible damage to reroll your entire dice pool."
            ),
          ),
        ),
      ),
      AbilityPool(
        1,
        List(
          AbilityTemplate(
            "Created Form",
            Status.Green,
            AbilityCategory.Inherent,
            _ => List(),
            List(
              "Reduce physical damage to yourself by 1 while you are in the Green zone, 2 while in the Yellow zone, and 3 while in the Red zone."
            ),
          ),
          AbilityTemplate(
            "Intentionality",
            Status.Green,
            AbilityCategory.Inherent,
            _ => List(),
            List(
              "Whenever you roll a 1 on one or more dice, you may reroll those dice. You must accept the result of the reroll."
            ),
          )
        ),
      )
    ),
    List(Power.inventions, Power.robotics, Power.signatureVehicle, Power.signatureWeapon) ++ Power.athleticPowers ++ Power.energyPowers ++ Power.intellectualPowers ++ Power.mobilityPowers ++ Power.selfControlPowers,
    List(d(10), d(8), d(8))
  )
end ArtificialBeing
